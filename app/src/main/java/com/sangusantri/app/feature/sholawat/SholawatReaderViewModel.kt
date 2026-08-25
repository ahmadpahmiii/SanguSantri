package com.sangusantri.app.feature.sholawat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.sync.ContentDetailSyncManager
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import com.sangusantri.app.feature.reader.ReaderUiAction
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns one Sholawat's reading content and its appearance preferences.
 *
 * Reading *position* is still not persisted (FR-SHL-007) — opening a sholawat always starts at the
 * top. Appearance is: the reader now shares the same [ReaderSettings] store as the Full and Guided
 * Readers, so font, size, line spacing and translation visibility carry across every Arabic reading
 * surface instead of resetting on each open. That reverses this feature's original stateless-v1
 * decision by product-owner approval; the two Sholawat-only switches (two-column, bait gap) live in
 * the same store for the same reason.
 *
 * The Arabic typeface is merged in from [QuranReaderSettingsRepository] rather than stored twice —
 * a single app-wide choice, exactly as the Full and Guided Readers handle it.
 */
@HiltViewModel(assistedFactory = SholawatReaderViewModel.Factory::class)
class SholawatReaderViewModel
@AssistedInject
constructor(
    @Assisted private val contentId: String,
    private val contentRepository: ContentRepository,
    private val quranReaderSettingsRepository: QuranReaderSettingsRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
    private val contentDetailSyncManager: ContentDetailSyncManager,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(contentId: String): SholawatReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private var loadJob: Job? = null

    private val mergedSettings =
        combine(
            readerSettingsRepository.observe(),
            quranReaderSettingsRepository.observe(),
        ) { settings, quranSettings ->
            settings.copy(arabicFont = quranSettings.arabicFont)
        }

    val uiState: StateFlow<SholawatReaderUiState> =
        combine(
            contentState,
            mergedSettings,
        ) { content, settings ->
            content.toUiState(settings)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SholawatReaderUiState.Loading,
        )

    init {
        loadContent()
    }

    fun retry() = loadContent()

    /**
     * Handles the subset of [ReaderUiAction] the shared `ReaderSettingsSheet` can send from within
     * the Sholawat reader (appearance only). Mirrors `GuidedReaderViewModel.onSettingsAction`: the
     * reader-specific actions are no-ops here because the settings sheet never dispatches them.
     */
    fun onSettingsAction(action: ReaderUiAction) {
        when (action) {
            is ReaderUiAction.SetArabicFont ->
                viewModelScope.launch { quranReaderSettingsRepository.setArabicFont(action.font) }

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

            is ReaderUiAction.SetThemeMode,
            is ReaderUiAction.ScrollPositionChanged,
            is ReaderUiAction.PersistPositionNow,
            ReaderUiAction.Retry,
            ReaderUiAction.SwitchToGuided,
            is ReaderUiAction.SwitchToGuidedAtStep,
                -> Unit
        }
    }

    fun setTwoColumn(enabled: Boolean) {
        viewModelScope.launch { readerSettingsRepository.setSholawatTwoColumn(enabled) }
    }

    fun setBaitGap(enabled: Boolean) {
        viewModelScope.launch { readerSettingsRepository.setSholawatBaitGap(enabled) }
    }

    // Room failures surface as unpredictable exception types; catching Exception here is the
    // deliberate boundary that turns any of them into RecoverableError instead of a crash
    // (matches ReaderViewModel's boundary). CancellationException is rethrown so
    // loadJob.cancel() is never swallowed.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun loadContent() {
        loadJob?.cancel()
        contentState.value = ContentState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val cached = contentRepository.getContentDetail(contentId)
                    if (cached == null) {
                        Log.w(TAG, "Sholawat content unavailable for id=$contentId: no catalogue row")
                        contentState.value = ContentState.Unavailable
                        return@launch
                    }

                    // Room first — the cached copy renders before any network call, and keeps
                    // rendering if that call never succeeds. Same contract as the Amaliyah
                    // reader; see ReaderViewModel.loadContent for the reasoning in full.
                    if (cached.steps.isNotEmpty()) {
                        contentState.value = ContentState.Available(cached)
                    }

                    val changed = contentDetailSyncManager.refresh(contentId, isSholawat = true)
                    val fresh = if (changed) contentRepository.getContentDetail(contentId) else cached

                    contentState.value =
                        if (fresh == null || fresh.steps.isEmpty()) {
                            Log.w(TAG, "Sholawat content unavailable for id=$contentId")
                            ContentState.Unavailable
                        } else {
                            ContentState.Available(fresh)
                        }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (unexpected: Exception) {
                    Log.e(TAG, "Sholawat content load failed for id=$contentId", unexpected)
                    contentState.value = ContentState.Error
                }
            }
    }

    private sealed interface ContentState {
        data object Loading : ContentState

        data class Available(
            val detail: ContentDetail,
        ) : ContentState

        data object Unavailable : ContentState

        data object Error : ContentState

        fun toUiState(settings: ReaderSettings): SholawatReaderUiState =
            when (this) {
                Loading -> SholawatReaderUiState.Loading
                Unavailable -> SholawatReaderUiState.Unavailable
                Error -> SholawatReaderUiState.RecoverableError
                is Available ->
                    SholawatReaderUiState.ContentAvailable(
                        title = detail.content.title,
                        steps = detail.steps,
                        layout = detail.content.layout,
                        settings = settings,
                    )
            }
    }

    private companion object {
        const val TAG = "SholawatReaderViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
