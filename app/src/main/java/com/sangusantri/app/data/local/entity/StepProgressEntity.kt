package com.sangusantri.app.data.local.entity

import androidx.room.Entity

/**
 * Interactive tasbih counter value for one step within one [ContentEntity] item (Milestone 4,
 * FR-006). Completion against the step's `repeatTarget` is derived at read time from content, not
 * stored redundantly here.
 */
@Entity(tableName = "step_progress", primaryKeys = ["contentId", "stepId"])
data class StepProgressEntity(
    val contentId: String,
    val stepId: String,
    val currentCount: Int,
    val updatedAtEpochMillis: Long,
)
