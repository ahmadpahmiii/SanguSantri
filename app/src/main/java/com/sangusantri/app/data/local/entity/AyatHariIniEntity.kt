package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One published day of the ayat-of-the-day schedule.
 *
 * **A reference, never scripture.** The row holds the surah and ayat numbers the editor chose; the
 * Arabic and translation are joined in from `quran_verses` at read time. Storing the text here would
 * create a second copy of Quran content that could drift from the Kemenag dataset, which
 * `CLAUDE.md` Content safety forbids.
 *
 * Keyed by [epochDay] rather than a formatted date string so the "which day is this" comparison is
 * an integer one and cannot go wrong across time zones or locales.
 */
@Entity(tableName = "ayat_hari_ini")
data class AyatHariIniEntity(
    @PrimaryKey val epochDay: Long,
    val surahNumber: Int,
    val ayatNumber: Int,
    val theme: String?,
)
