package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sangusantri.app.data.local.entity.TasbihSessionEntity.Companion.SINGLETON_ID
import com.sangusantri.app.domain.model.TasbihTargetPreset

/**
 * The single active Standalone Tasbih session (0.0.2). Always exactly one row, [id] fixed at
 * [SINGLETON_ID] — this is "the current session," not a session history (see
 * [TasbihHistoryEntity] for archived sessions).
 */
@Entity(tableName = "tasbih_sessions")
data class TasbihSessionEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val currentCount: Int,
    val targetValue: Int?,
    val targetPreset: TasbihTargetPreset,
    val sessionName: String?,
    val startedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
