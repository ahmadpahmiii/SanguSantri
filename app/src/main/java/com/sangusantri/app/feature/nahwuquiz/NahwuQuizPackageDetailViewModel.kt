package com.sangusantri.app.feature.nahwuquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** `Detail Paket` — one package's own status, reused as-is for design spec states 3 (normal), 13
 * (`Bank Soal Kosong`, [NahwuQuizPackageSummary.status] `UNAVAILABLE`) and — combined with
 * [NahwuQuizPackagesUiState.ContentUnavailable]'s sibling handling — 15 if the id resolves to
 * nothing at all. */
sealed interface NahwuQuizPackageDetailUiState {
    data object Loading : NahwuQuizPackageDetailUiState

    data class Content(
        val summary: NahwuQuizPackageSummary,
    ) : NahwuQuizPackageDetailUiState

    data object NotFound : NahwuQuizPackageDetailUiState
}

@HiltViewModel(assistedFactory = NahwuQuizPackageDetailViewModel.Factory::class)
class NahwuQuizPackageDetailViewModel
@AssistedInject
constructor(
    @Assisted private val packageId: String,
    nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(packageId: String): NahwuQuizPackageDetailViewModel
    }

    val uiState: StateFlow<NahwuQuizPackageDetailUiState> =
        nahwuQuizRepository
            .observePackageSummaries()
            .map { summaries -> summaries.find { it.quizPackage.id == packageId } }
            .map { summary ->
                if (summary == null) {
                    NahwuQuizPackageDetailUiState.NotFound
                } else {
                    NahwuQuizPackageDetailUiState.Content(summary)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizPackageDetailUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
