package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached tafsir for one ayat, keyed by the remote ayat id (QUR-FR-013). Deliberately not
 * foreign-keyed to [QuranVerseEntity] because it is fetched and cached independently on demand.
 * A successful versioned corpus replacement explicitly clears this table: a future source update
 * may change remote ids, so retaining those associations would be unsafe.
 */
@Entity(tableName = "quran_tafsir")
data class QuranTafsirEntity(
    @PrimaryKey val remoteAyatId: Long,
    val ringkas: String,
    val tahlili: String,
    val cachedAtEpochMillis: Long,
)
