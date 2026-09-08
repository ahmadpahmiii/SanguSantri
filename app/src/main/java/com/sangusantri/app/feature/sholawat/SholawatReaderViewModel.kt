package com.sangusantri.app.feature.sholawat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.core.result.Resource
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
class SholawatReaderViewModel @AssistedInject constructor(
    @Assisted private val contentId: String,
    private val contentRepository: ContentRepository,
    private val quranReaderSettingsRepository: QuranReaderSettingsRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
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

    /** One offline-first stream from the repository. Same contract as the Amaliyah reader; see
     * [com.sangusantri.app.feature.reader.ReaderViewModel.loadContent] for the reasoning in full. */
    private fun loadContent() {
        loadJob?.cancel()
        contentState.value = ContentState.Loading
        loadJob =
            contentRepository
                .observeContentDetail(contentId)
                .onEach { contentState.value = it.toContentState() }
                .catch { failure ->
                    Log.e(TAG, "Sholawat content load failed for id=$contentId", failure)
                    contentState.value = ContentState.Error
                }.launchIn(viewModelScope)
    }

    private fun Resource<ContentDetail>.toContentState(): ContentState {
        val detail = data
        return when {
            detail != null && detail.steps.isNotEmpty() -> ContentState.Available(detail)
            this is Resource.Loading -> ContentState.Loading
            else -> {
                Log.w(TAG, "Sholawat content unavailable for id=$contentId")
                ContentState.Unavailable
            }
        }
    }

    private sealed interface ContentState {
        data object Loading : ContentState

        data class Available(val detail: ContentDetail) : ContentState

        data object Unavailable : ContentState

        data object Error : ContentState

        fun toUiState(settings: ReaderSettings): SholawatReaderUiState = when (this) {
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
