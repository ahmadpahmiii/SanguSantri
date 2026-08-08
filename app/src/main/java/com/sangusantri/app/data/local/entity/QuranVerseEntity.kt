package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * One official LPMQ Kemenag ayat record, keyed by the stable local `(surahNumber, ayatNumber)`
 * identity rather than the remote `id` (`docs/engineering/CONTENT_MODEL.md`). [remoteId] stays
 * unique for tafsir lookup (QUR-FR-013) but is deliberately not the primary key, since bookmarks
 * and the last-read position must survive a source refresh by `(surah, ayat)` identity even if a
 * future refresh were to change remote ids.
 *
 * The API's `teks` Latin transliteration field is deliberately absent here — it must never be
 * persisted (QUR-FR-009).
 */
@Entity(
    tableName = "quran_verses",
    primaryKeys = ["surahNumber", "ayatNumber"],
    foreignKeys = [
        ForeignKey(
            entity = QuranSurahEntity::class,
            parentColumns = ["number"],
            childColumns = ["surahNumber"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["surahNumber"]),
        Index(value = ["page"]),
    ],
)
data class QuranVerseEntity(
    val surahNumber: Int,
    val ayatNumber: Int,
    val remoteId: Long,
    val juz: Int,
    val page: Int,
    val arabicText: String,
    val arabicTextNoHarakat: String,
    val translation: String,
    val note: String,
    val footnoteNumber: String,
    val footnoteText: String,
)
