package com.sangusantri.app.feature.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.sync.ContentDetailSyncManager
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.model.hasGuidedMode
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns Full Reader screen state (Milestone 3). Loads the content item once (ADR 0015: a flat
 * [ContentDetail], no variant/version resolution any more), combines it with live
 * [ReaderSettings] so appearance changes apply without a reload, and persists the visible reading
 * position — debounced, and flushed immediately on [ReaderUiAction.PersistPositionNow] (dispatched
 * on `Lifecycle.Event.ON_STOP`) — per content id.
 *
 * The Full Reader genuinely depends on all of these: content, saved position, its own settings, the
 * shared Arabic-font setting, the guided-mode handover, and now the detail refresh that keeps an
 * open item current. Collapsing them into a wrapper would hide the dependencies, not remove them.
 */
@Suppress("LongParameterList")
@OptIn(FlowPreview::class)
@HiltViewModel(assistedFactory = ReaderViewModel.Factory::class)
class ReaderViewModel
@AssistedInject
constructor(
    @Assisted private val contentId: String,
    private val contentRepository: ContentRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
    private val guidedReadingRepository: GuidedReadingRepository,
    private val quranReaderSettingsRepository: QuranReaderSettingsRepository,
    private val contentDetailSyncManager: ContentDetailSyncManager,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(contentId: String): ReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private var loadJob: Job? = null
    private var lastKnownItemIndex = 0

    private val scrollPositionUpdates =
        MutableSharedFlow<ScrollPosition>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val _switchToGuidedReady = MutableStateFlow(false)
    val switchToGuidedReady: StateFlow<Boolean> = _switchToGuidedReady

    /** [ReaderSettings] with `arabicFont` overlaid from the shared [quranReaderSettingsRepository] —
     * see that field's KDoc for why it isn't part of [readerSettingsRepository]'s own store. */
    private val mergedSettings: Flow<ReaderSettings> =
        combine(
            readerSettingsRepository.observe(),
            quranReaderSettingsRepository.observe(),
        ) { settings, quranSettings ->
            settings.copy(arabicFont = quranSettings.arabicFont)
        }

    val uiState: StateFlow<ReaderUiState> =
        combine(contentState, mergedSettings) { content, settings ->
            content.toUiState(settings)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ReaderUiState.Loading,
        )

    /**
     * Settings observed independently of [uiState] so the settings sheet always reflects the
     * real persisted values, even while content is loading, unavailable, or errored.
     */
    val settings: StateFlow<ReaderSettings> =
        mergedSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ReaderSettings(),
        )

    init {
        loadContent()
        scrollPositionUpdates
            .debounce(POSITION_SAVE_DEBOUNCE_MILLIS)
            .onEach { persistPosition(it) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ReaderUiAction) {
        when (action) {
            is ReaderUiAction.SetThemeMode ->
                viewModelScope.launch { quranReaderSettingsRepository.setThemeMode(action.mode) }

            is ReaderUiAction.SetArabicFont ->
                viewModelScope.launch { quranReaderSettingsRepository.setArabicFont(action.font) }

            is ReaderUiAction.ScrollPositionChanged -> {
                lastKnownItemIndex = action.itemIndex
                scrollPositionUpdates.tryEmit(ScrollPosition(action.itemIndex, action.itemOffset))
            }

            is ReaderUiAction.PersistPositionNow ->
                viewModelScope.launch {
                    persistPosition(ScrollPosition(action.itemIndex, action.itemOffset))
                }

            is ReaderUiAction.SetArabicFontSize ->
                viewModelScope.launch { readerSettingsRepository.setArabicFontSize(action.sp) }

            is ReaderUiAction.SetTranslationFontSize ->
                viewModelScope.launch { readerSettingsRepository.setTranslationFontSize(action.sp) }

            is ReaderUiAction.SetArabicLineSpacing ->
                viewModelScope.launch { readerSettingsRepository.setArabicLineSpacing(action.multiplier) }

            is ReaderUiAction.SetTranslationLineSpacing ->
                viewModelScope.launch { readerSettingsRepository.setTranslationLineSpacing(action.multiplier) }

            is ReaderUiAction.SetShowTranslation ->
                viewModelScope.launch { readerSettingsRepository.setShowTranslation(action.show) }

            ReaderUiAction.Retry -> loadContent()
            ReaderUiAction.SwitchToGuided -> onSwitchToGuided()
            is ReaderUiAction.SwitchToGuidedAtStep -> onSwitchToGuidedAtStep(action.stepId)
        }
    }

    /** Overflow-menu mode switch (FR-016) — targets the currently visible step. */
    private fun onSwitchToGuided() {
        val detail = (contentState.value as? ContentState.Available)?.detail ?: return
        val clampedIndex = lastKnownItemIndex.coerceIn(0, detail.steps.lastIndex)
        switchToGuided(detail, detail.steps[clampedIndex].id)
    }

    /** Full Reader repetition shortcut (FR-018) — targets the exact step whose pill was tapped. */
    private fun onSwitchToGuidedAtStep(stepId: String) {
        val detail = (contentState.value as? ContentState.Available)?.detail ?: return
        switchToGuided(detail, stepId)
    }

    /**
     * Writes [stepId] directly into the existing per-content [GuidedReadingSession] row
     * (preserving any completion already recorded there) instead of inventing a second progress
     * model — the Guided Reader then simply restores its usual session state on load and finds
     * this step already current.
     */
    private fun switchToGuided(
        detail: ContentDetail,
        stepId: String,
    ) {
        viewModelScope.launch {
            val existingSession = guidedReadingRepository.getSession(detail.content.id)
            guidedReadingRepository.saveSession(
                GuidedReadingSession(
                    contentId = detail.content.id,
                    currentStepId = stepId,
                    lastOpenedAtEpochMillis = System.currentTimeMillis(),
                    completedAtEpochMillis = existingSession?.completedAtEpochMillis,
                    startedAtEpochMillis = existingSession?.startedAtEpochMillis ?: System.currentTimeMillis(),
                ),
            )
            readerSettingsRepository.setLastReaderMode(ReaderMode.GUIDED)
            _switchToGuidedReady.value = true
        }
    }

    // Room/DataStore failures surface as unpredictable exception types; catching Exception here is
    // the deliberate boundary that turns any of them into RecoverableError instead of a crash or a
    // raw error string (OFFLINE_FIRST.md "Application resilience"). CancellationException is
    // rethrown so structured concurrency (e.g. loadJob.cancel()) is never swallowed.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun loadContent() {
        loadJob?.cancel()
        contentState.value = ContentState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val cached = contentRepository.getContentDetail(contentId)
                    if (cached == null) {
                        Log.w(TAG, "Content unavailable for id=$contentId: no catalogue row")
                        contentState.value = ContentState.Unavailable
                        return@launch
                    }

                    // Room first, always. Whatever is already cached renders now; the network
                    // never gates the reader (PRD 12.1). An item opened before has its steps
                    // here, so this is the frame the reader actually sees.
                    if (cached.steps.isNotEmpty()) {
                        contentState.value = available(cached)
                    }

                    // Then refresh, every open. An unchanged item costs a 304 and no body, and
                    // this is the only thing that brings a correction published since the last
                    // open to a reader who never closes the app. An item whose detail has never
                    // been fetched — a row the list created but nobody opened yet — has no steps
                    // to show, so for it this fetch is the load, and its failure is visible.
                    val changed = contentDetailSyncManager.refresh(contentId, cached.content.isSholawat)
                    val fresh = if (changed) contentRepository.getContentDetail(contentId) else cached

                    contentState.value =
                        if (fresh == null || fresh.steps.isEmpty()) {
                            Log.w(
                                TAG,
                                "Content unavailable for id=$contentId: " +
                                    "stepCount=${fresh?.steps?.size ?: 0}, refreshChanged=$changed",
                            )
                            ContentState.Unavailable
                        } else {
                            available(fresh)
                        }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (unexpected: Exception) {
                    Log.e(TAG, "Reader content load failed for id=$contentId", unexpected)
                    contentState.value = ContentState.Error
                }
            }
    }

    private suspend fun available(detail: ContentDetail): ContentState.Available {
        val restored = readingPositionRepository.getPosition(contentId)
        val position = validateRestoredPosition(restored, detail.steps.size)
        lastKnownItemIndex = position.itemIndex
        return ContentState.Available(detail = detail, restoredPosition = position)
    }

    private suspend fun persistPosition(position: ScrollPosition) {
        val id = (contentState.value as? ContentState.Available)?.detail?.content?.id ?: return
        readingPositionRepository.savePosition(
            ReadingPosition(
                contentId = id,
                itemIndex = position.itemIndex,
                itemOffset = position.itemOffset,
                lastOpenedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    private fun validateRestoredPosition(
        position: ReadingPosition?,
        stepCount: Int,
    ): ScrollPosition {
        if (position == null || position.itemIndex < 0 || position.itemIndex >= stepCount) {
            return ScrollPosition(itemIndex = 0, itemOffset = 0)
        }
        return ScrollPosition(itemIndex = position.itemIndex, itemOffset = position.itemOffset.coerceAtLeast(0))
    }

    private sealed interface ContentState {
        data object Loading : ContentState

        data class Available(
            val detail: ContentDetail,
            val restoredPosition: ScrollPosition,
        ) : ContentState

        data object Unavailable : ContentState

        data object Error : ContentState

        fun toUiState(settings: ReaderSettings): ReaderUiState =
            when (this) {
                Loading -> ReaderUiState.Loading
                Unavailable -> ReaderUiState.ContentUnavailable
                Error -> ReaderUiState.RecoverableError
                is Available ->
                    ReaderUiState.ContentAvailable(
                        title = detail.content.title,
                        contentId = detail.content.id,
                        steps = detail.steps,
                        settings = settings,
                        initialItemIndex = restoredPosition.itemIndex,
                        initialItemOffset = restoredPosition.itemOffset,
                        sourceName = detail.content.sourceName,
                        hasGuidedMode = detail.steps.hasGuidedMode(),
                    )
            }
    }

    private data class ScrollPosition(
        val itemIndex: Int,
        val itemOffset: Int,
    )

    private companion object {
        const val TAG = "ReaderViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val POSITION_SAVE_DEBOUNCE_MILLIS = 600L
    }
}
