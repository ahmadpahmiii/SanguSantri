package com.sangusantri.app.domain.model

/**
 * Guided Reader progress for one [Content] item (Milestone 4, PRD FR-005/FR-007). Mirrors
 * [ReadingPosition]'s per-content scope, but tracks the current step and completion instead of a
 * scroll position — Full Reader and Guided Reader intentionally keep separate progress records
 * (`docs/engineering/CONTENT_MODEL.md`).
 */
data class GuidedReadingSession(
    val contentId: String,
    val currentStepId: String,
    val lastOpenedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    /**
     * When this session was first created — set once and preserved on every subsequent save
     * (never overwritten on step moves), so `completedAtEpochMillis - startedAtEpochMillis` is a
     * real, non-fabricated duration for Aktivitas' completion history (`0.0.3`).
     */
    val startedAtEpochMillis: Long,
)
