package com.sangusantri.app.domain.model

/**
 * One quiz-taking session (`0.0.5`) — created by `NahwuQuizRepository.getOrCreateActiveAttempt`,
 * which resumes the existing incomplete attempt for a package if one exists, or starts a fresh
 * one otherwise (this also naturally implements "Ulangi kuis": once the prior attempt is
 * complete, the next call creates a new one). [totalCount] is a snapshot of the package's
 * question count at the moment the attempt started, so a completed attempt's score stays stable
 * in `Riwayat Skor Individual` even if the bundled bank is ever replaced later.
 */
data class NahwuQuizAttempt(
    val id: String,
    val packageId: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val currentQuestionIndex: Int,
    val correctCount: Int,
    val totalCount: Int,
) {
    val isCompleted: Boolean get() = completedAtEpochMillis != null

    val scorePercent: Int get() = if (totalCount == 0) 0 else (correctCount * 100) / totalCount

    val durationMillis: Long? get() = completedAtEpochMillis?.let { it - startedAtEpochMillis }
}

/** The single soonest-started, still-incomplete attempt across every package — backs the
 * "Lanjutkan kuis" card on `Nahwu Quiz / Landing` (state 12), which is global, not per-package. */
data class NahwuQuizActiveAttempt(
    val attemptId: String,
    val packageId: String,
    val packageTitle: String,
    val answeredCount: Int,
    val totalCount: Int,
)
