package com.sangusantri.app.domain.model

/** One bundled Nahwu Quiz question package (`0.0.5`) — catalog data only, no attempt/progress
 * information (see [NahwuQuizPackageSummary] for that). */
data class NahwuQuizPackage(
    val id: String,
    val title: String,
    val description: String,
    val order: Int,
    val isActive: Boolean,
    val questionCount: Int,
)

/** A package a user never attempted has no progress to show; [UNAVAILABLE] overrides the other
 * three whenever [NahwuQuizPackage.questionCount] is zero, regardless of any attempt. */
enum class NahwuQuizPackageStatus {
    NEW,
    IN_PROGRESS,
    COMPLETED,
    UNAVAILABLE,
}

/**
 * [NahwuQuizPackage] plus its attempt-derived display state — the one model both `Daftar Paket`'s
 * list cards and `Detail Paket` render from (`docs/design/design-export/future-releases/
 * 05-release-0.0.5-nahwu-quiz.md`, states 2/3). [answeredCount] is non-null exactly when
 * [status] is [NahwuQuizPackageStatus.IN_PROGRESS] or [NahwuQuizPackageStatus.COMPLETED] — the
 * progress bar/"`n`/`total` selesai" row is hidden otherwise (never opened yet, or unavailable).
 */
data class NahwuQuizPackageSummary(
    val quizPackage: NahwuQuizPackage,
    val status: NahwuQuizPackageStatus,
    val answeredCount: Int?,
)
