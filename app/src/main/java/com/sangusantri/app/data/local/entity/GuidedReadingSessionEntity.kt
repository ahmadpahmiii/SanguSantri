package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Guided Reader progress for one immutable content version (Milestone 4, PRD FR-005/FR-006). One
 * row per [versionId] — mirrors [ReadingPositionEntity]'s per-version keying convention, kept as a
 * separate table because guided-mode state (current step, completion) has no Full Reader
 * equivalent (`docs/engineering/CONTENT_MODEL.md`).
 */
@Entity(tableName = "guided_reading_sessions")
data class GuidedReadingSessionEntity(
    @PrimaryKey val versionId: String,
    val currentStepId: String,
    val lastOpenedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val startedAtEpochMillis: Long,
)
