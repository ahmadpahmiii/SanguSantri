package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached tafsir for one ayat, keyed by the remote ayat id (QUR-FR-013). Deliberately not
 * foreign-keyed to [QuranVerseEntity] — tafsir is fetched and cached independently of a Quran
 * source refresh, on demand, and must not be cascade-deleted when verses are replaced.
 */
@Entity(tableName = "quran_tafsir")
data class QuranTafsirEntity(
    @PrimaryKey val remoteAyatId: Long,
    val ringkas: String,
    val tahlili: String,
    val cachedAtEpochMillis: Long,
)
