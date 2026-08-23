package com.sangusantri.app.feature.sholawat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.sync.ContentDetailSyncManager
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
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
 * Owns one Sholawat's reading content. Deliberately stateless (0.0.8 scope): no reading position,
 * no font-size persistence, no [com.sangusantri.app.domain.model.ReaderSettings] — the
 * Arabic-only/with-translation toggle lives as local Compose state in
 * [SholawatReaderScreen], reset every time the screen is opened fresh.
 *
 * The Arabic typeface is the one exception: it is a single app-wide choice made in the Quran
 * reader's settings, so it is observed from [QuranReaderSettingsRepository] here exactly as the
 * Full and Guided Readers do, rather than given the Sholawat reader a picker of its own.
 */
@HiltViewModel(assistedFactory = SholawatReaderViewModel.Factory::class)
class SholawatReaderViewModel
@AssistedInject
constructor(
    @Assisted private val contentId: String,
    private val contentRepository: ContentRepository,
    private val quranReaderSettingsRepository: QuranReaderSettingsRepository,
    private val contentDetailSyncManager: ContentDetailSyncManager,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(contentId: String): SholawatReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private var loadJob: Job? = null

    val uiState: StateFlow<SholawatReaderUiState> =
        combine(
            contentState,
            quranReaderSettingsRepository.observe(),
        ) { content, quranSettings ->
            content.toUiState(quranSettings.arabicFont)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SholawatReaderUiState.Loading,
        )

    init {
        loadContent()
    }

    fun retry() = loadContent()

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

        fun toUiState(arabicFont: QuranArabicFont): SholawatReaderUiState =
            when (this) {
                Loading -> SholawatReaderUiState.Loading
                Unavailable -> SholawatReaderUiState.Unavailable
                Error -> SholawatReaderUiState.RecoverableError
                is Available ->
                    SholawatReaderUiState.ContentAvailable(
                        title = detail.content.title,
                        steps = detail.steps,
                        arabicFont = arabicFont,
                    )
            }
    }

    private companion object {
        const val TAG = "SholawatReaderViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
