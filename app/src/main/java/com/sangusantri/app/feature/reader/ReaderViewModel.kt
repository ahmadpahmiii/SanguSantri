package com.sangusantri.app.feature.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.repository.ContentRepository
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
 * Owns Full Reader screen state (Milestone 3). Loads the amaliyah's default published version
 * once, combines it with live [ReaderSettings] so appearance changes apply without a reload, and
 * persists the visible reading position — debounced, and flushed immediately on
 * [ReaderUiAction.PersistPositionNow] (dispatched on `Lifecycle.Event.ON_STOP`) — per content
 * version id.
 */
@OptIn(FlowPreview::class)
@HiltViewModel(assistedFactory = ReaderViewModel.Factory::class)
class ReaderViewModel
@AssistedInject
constructor(
    @Assisted private val amaliyahSlug: String,
    private val contentRepository: ContentRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(amaliyahSlug: String): ReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private var loadJob: Job? = null

    private val scrollPositionUpdates =
        MutableSharedFlow<ScrollPosition>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val uiState: StateFlow<ReaderUiState> =
        combine(contentState, readerSettingsRepository.observe()) { content, settings ->
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
        readerSettingsRepository.observe().stateIn(
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
            is ReaderUiAction.ScrollPositionChanged ->
                scrollPositionUpdates.tryEmit(ScrollPosition(action.itemIndex, action.itemOffset))

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
                contentState.value =
                    try {
                        val amaliyah = contentRepository.getAmaliyahBySlug(amaliyahSlug)
                        val detail = contentRepository.getDefaultVersionDetail(amaliyahSlug)
                        if (amaliyah == null || detail == null || detail.steps.isEmpty()) {
                            Log.w(
                                TAG,
                                "Content unavailable for slug=$amaliyahSlug: " +
                                    "amaliyahFound=${amaliyah != null}, activeVersionFound=${detail != null}, " +
                                    "stepCount=${detail?.steps?.size ?: 0}",
                            )
                            ContentState.Unavailable
                        } else {
                            val restored = readingPositionRepository.getPosition(detail.version.id)
                            ContentState.Available(
                                amaliyahTitleId = amaliyah.titleId,
                                detail = detail,
                                restoredPosition = validateRestoredPosition(restored, detail.steps.size),
                            )
                        }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (unexpected: Exception) {
                        Log.e(TAG, "Reader content load failed for slug=$amaliyahSlug", unexpected)
                        ContentState.Error
                    }
            }
    }

    private suspend fun persistPosition(position: ScrollPosition) {
        val versionId = (contentState.value as? ContentState.Available)?.detail?.version?.id ?: return
        readingPositionRepository.savePosition(
            ReadingPosition(
                versionId = versionId,
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
            val amaliyahTitleId: String,
            val detail: AmaliyahVersionDetail,
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
                        amaliyahTitleId = amaliyahTitleId,
                        versionId = detail.version.id,
                        steps = detail.steps,
                        settings = settings,
                        initialItemIndex = restoredPosition.itemIndex,
                        initialItemOffset = restoredPosition.itemOffset,
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
