package com.sangusantri.app.feature.nahwuquiz

import com.sangusantri.app.domain.model.NahwuQuizOptionKey
import com.sangusantri.app.domain.model.NahwuQuizQuestion

/**
 * `Pertanyaan`/`Jawaban Dipilih`/`Feedback Benar`/`Feedback Salah` (design spec states 5-8) — one
 * screen, four visual states driven by [selectedOption]/[isSubmitted]. [correctOption] is `null`
 * until [isSubmitted] is true — the security annotation on the design spec ("no sensitive
 * answer-key data in raw UI state") is honoured at this boundary even though the underlying
 * storage already holds the full question locally.
 */
sealed interface NahwuQuizSessionUiState {
    data object Loading : NahwuQuizSessionUiState

    data class QuestionVisible(
        val packageTitle: String,
        val questionIndex: Int,
        val questionCount: Int,
        val question: NahwuQuizQuestion,
        val selectedOption: NahwuQuizOptionKey?,
        val isSubmitted: Boolean,
        val isCorrect: Boolean?,
        val correctOption: NahwuQuizOptionKey?,
    ) : NahwuQuizSessionUiState {
        val canSubmit: Boolean get() = !isSubmitted && selectedOption != null
        val isLastQuestion: Boolean get() = questionIndex == questionCount - 1
    }

    /** A specific package with zero bundled questions (design spec state 13) — reachable only if
     * the user navigates here directly with a stale/invalid id, since `Detail Paket` never offers
     * a "Mulai" action for an unavailable package. */
    data object ContentUnavailable : NahwuQuizSessionUiState

    data object RecoverableError : NahwuQuizSessionUiState

    /** Terminal state — the route reacts to this by navigating to `Hasil Kuis`, replacing this
     * entry so back from the result screen never returns mid-quiz. */
    data class Completed(
        val attemptId: String,
    ) : NahwuQuizSessionUiState
}
