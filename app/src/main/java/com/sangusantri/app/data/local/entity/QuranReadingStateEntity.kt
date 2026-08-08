package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single global last-read position (QUR-FR-011) — a singleton row at [SINGLETON_ID], same
 * fixed-id convention as `TasbihSessionEntity`. Deliberately not foreign-keyed to
 * [QuranVerseEntity] for the same reason as [QuranBookmarkEntity].
 */
@Entity(tableName = "quran_reading_state")
data class QuranReadingStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val surahNumber: Int,
    val ayatNumber: Int,
    val page: Int,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
