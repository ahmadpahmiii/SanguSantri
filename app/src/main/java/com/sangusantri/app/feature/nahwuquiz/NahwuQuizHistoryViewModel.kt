package com.sangusantri.app.feature.nahwuquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** `Riwayat Skor Individual` (design spec state 10) — mirrors `TasbihHistoryUiState`'s
 * Loading/Empty/Filled naming. */
sealed interface NahwuQuizHistoryUiState {
    data object Loading : NahwuQuizHistoryUiState

    data class Filled(
        val packageTitle: String,
        val attempts: List<NahwuQuizAttempt>,
    ) : NahwuQuizHistoryUiState

    data object Empty : NahwuQuizHistoryUiState
}

@HiltViewModel(assistedFactory = NahwuQuizHistoryViewModel.Factory::class)
class NahwuQuizHistoryViewModel
@AssistedInject
constructor(
    @Assisted private val packageId: String,
    private val nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(packageId: String): NahwuQuizHistoryViewModel
    }

    val uiState: StateFlow<NahwuQuizHistoryUiState> =
        nahwuQuizRepository
            .observeCompletedAttempts(packageId)
            .map { attempts -> if (attempts.isEmpty()) NahwuQuizHistoryUiState.Empty else buildFilled(attempts) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizHistoryUiState.Loading,
            )

    private suspend fun buildFilled(attempts: List<NahwuQuizAttempt>): NahwuQuizHistoryUiState.Filled {
        val title = nahwuQuizRepository.getPackage(packageId)?.title.orEmpty()
        return NahwuQuizHistoryUiState.Filled(packageTitle = title, attempts = attempts)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
