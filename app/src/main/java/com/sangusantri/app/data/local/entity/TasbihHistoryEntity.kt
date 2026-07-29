package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One archived Standalone Tasbih session (0.0.2), inserted when a session is confirmed-reset. */
@Entity(tableName = "tasbih_history")
data class TasbihHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionName: String?,
    val targetValue: Int?,
    val finalCount: Int,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long,
)
