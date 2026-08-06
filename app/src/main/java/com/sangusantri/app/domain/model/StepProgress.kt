package com.sangusantri.app.domain.model

/** The interactive tasbih counter's current value for one step of one [Content] item (FR-006). */
data class StepProgress(
    val contentId: String,
    val stepId: String,
    val currentCount: Int,
    val updatedAtEpochMillis: Long,
)
