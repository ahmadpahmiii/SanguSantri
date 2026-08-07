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

/** `Hasil Kuis` (design spec state 9). [previousScorePercent] is `null` for a package's first-ever
 * completed attempt, hiding the optional score-delta line. */
sealed interface NahwuQuizResultUiState {
    data object Loading : NahwuQuizResultUiState

    data class Content(
        val packageId: String,
        val packageTitle: String,
        val scorePercent: Int,
        val correctCount: Int,
        val totalCount: Int,
        val previousScorePercent: Int?,
    ) : NahwuQuizResultUiState

    data object NotFound : NahwuQuizResultUiState
}

@HiltViewModel(assistedFactory = NahwuQuizResultViewModel.Factory::class)
class NahwuQuizResultViewModel
@AssistedInject
constructor(
    @Assisted private val attemptId: String,
    private val nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(attemptId: String): NahwuQuizResultViewModel
    }

    val uiState: StateFlow<NahwuQuizResultUiState> =
        nahwuQuizRepository
            .observeAttempt(attemptId)
            .map { attempt -> attempt?.let { buildContent(it) } ?: NahwuQuizResultUiState.NotFound }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizResultUiState.Loading,
            )

    private suspend fun buildContent(attempt: NahwuQuizAttempt): NahwuQuizResultUiState.Content {
        val quizPackage = nahwuQuizRepository.getPackage(attempt.packageId)
        val previousScorePercent = nahwuQuizRepository.getPreviousScorePercent(attempt.packageId, attempt.id)
        return NahwuQuizResultUiState.Content(
            packageId = attempt.packageId,
            packageTitle = quizPackage?.title.orEmpty(),
            scorePercent = attempt.scorePercent,
            correctCount = attempt.correctCount,
            totalCount = attempt.totalCount,
            previousScorePercent = previousScorePercent,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
