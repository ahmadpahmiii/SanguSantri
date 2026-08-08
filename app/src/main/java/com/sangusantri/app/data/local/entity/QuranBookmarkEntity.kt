package com.sangusantri.app.data.local.entity

import androidx.room.Entity

/**
 * One local, ayat-level bookmark (QUR-FR-012). Deliberately not foreign-keyed to
 * [QuranVerseEntity]: a source refresh replaces `quran_verses` wholesale (delete-then-insert), and
 * a cascading foreign key would wipe every bookmark on the same refresh it is required to survive
 * (QUR-FR-012's "refresh must preserve bookmarks by `(surah, ayat)` identity").
 */
@Entity(tableName = "quran_bookmarks", primaryKeys = ["surahNumber", "ayatNumber"])
data class QuranBookmarkEntity(
    val surahNumber: Int,
    val ayatNumber: Int,
    val createdAtEpochMillis: Long,
)
