package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One archived Guided Reader completion (Aktivitas, `0.0.3`). Deliberately has no foreign key to
 * `amaliyah_versions` — [amaliyahTitleId]/[versionNumber] are snapshots, so this row survives
 * unchanged when a content package is later replaced or the amaliyah renamed (unlike
 * `guided_reading_sessions`, which is deleted on version replacement).
 */
@Entity(tableName = "amaliyah_completion_events")
data class AmaliyahCompletionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amaliyahSlug: String,
    val amaliyahTitleId: String,
    val versionNumber: Int,
    val completedAtEpochMillis: Long,
    val durationMillis: Long,
)
