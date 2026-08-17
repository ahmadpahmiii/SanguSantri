package com.sangusantri.app.feature.sholawat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns one Sholawat's reading content. Deliberately stateless (0.0.8 scope): no reading position,
 * no font-size persistence, no [com.sangusantri.app.domain.model.ReaderSettings] — the
 * Arabic-only/with-translation toggle lives as local Compose state in
 * [SholawatReaderScreen], reset every time the screen is opened fresh.
 */
@HiltViewModel(assistedFactory = SholawatReaderViewModel.Factory::class)
class SholawatReaderViewModel
@AssistedInject
constructor(
    @Assisted private val contentId: String,
    private val contentRepository: ContentRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(contentId: String): SholawatReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private var loadJob: Job? = null

    val uiState: StateFlow<SholawatReaderUiState> =
        contentState.map(ContentState::toUiState).stateIn(
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
                contentState.value =
                    try {
                        val detail = contentRepository.getContentDetail(contentId)
                        if (detail == null || detail.steps.isEmpty()) {
                            Log.w(TAG, "Sholawat content unavailable for id=$contentId")
                            ContentState.Unavailable
                        } else {
                            ContentState.Available(detail)
                        }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (unexpected: Exception) {
                        Log.e(TAG, "Sholawat content load failed for id=$contentId", unexpected)
                        ContentState.Error
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

        fun toUiState(): SholawatReaderUiState =
            when (this) {
                Loading -> SholawatReaderUiState.Loading
                Unavailable -> SholawatReaderUiState.Unavailable
                Error -> SholawatReaderUiState.RecoverableError
                is Available ->
                    SholawatReaderUiState.ContentAvailable(
                        title = detail.content.title,
                        steps = detail.steps,
                    )
            }
    }

    private companion object {
        const val TAG = "SholawatReaderViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
