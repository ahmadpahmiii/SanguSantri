package com.sangusantri.app.domain.model

/**
 * Guided Reader progress for one immutable content version (Milestone 4, FR-005/FR-007). Mirrors
 * [ReadingPosition]'s per-version scope, but tracks the current step and completion instead of a
 * scroll position — Full Reader and Guided Reader intentionally keep separate progress records
 * (`docs/engineering/CONTENT_MODEL.md`).
 */
data class GuidedReadingSession(
    val versionId: String,
    val currentStepId: String,
    val lastOpenedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
)
