package com.sangusantri.app.feature.nahwuquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.local.nahwuquiz.NahwuQuizBootstrapper
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** `Daftar Paket`. An empty summary list means the bundled question bank never loaded (corrupt or
 * missing local data) — the same "no content vs. content unreachable" collapse the readers already
 * use for [com.sangusantri.app.feature.reader.ReaderUiState.ContentUnavailable]; there is no
 * user-facing distinction between "genuinely zero packages" and "bootstrap failed", since a real
 * bundled release never ships an intentionally empty bank. */
sealed interface NahwuQuizPackagesUiState {
    data object Loading : NahwuQuizPackagesUiState

    data class Content(
        val summaries: List<NahwuQuizPackageSummary>,
    ) : NahwuQuizPackagesUiState

    data object ContentUnavailable : NahwuQuizPackagesUiState
}

@HiltViewModel
class NahwuQuizPackagesViewModel
@Inject
constructor(
    private val nahwuQuizRepository: NahwuQuizRepository,
    private val nahwuQuizBootstrapper: NahwuQuizBootstrapper,
) : ViewModel() {
    val uiState: StateFlow<NahwuQuizPackagesUiState> =
        nahwuQuizRepository
            .observePackageSummaries()
            .map { summaries ->
                if (summaries.isEmpty()) {
                    NahwuQuizPackagesUiState.ContentUnavailable
                } else {
                    NahwuQuizPackagesUiState.Content(summaries)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizPackagesUiState.Loading,
            )

    /** Re-runs the bundled-asset import — a real recovery action, not a fake retry: a prior
     * failed attempt leaves the package table empty, so this genuinely retries the read/parse
     * that failed the first time. */
    fun retry() {
        viewModelScope.launch { nahwuQuizBootstrapper.bootstrapIfNeeded() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
