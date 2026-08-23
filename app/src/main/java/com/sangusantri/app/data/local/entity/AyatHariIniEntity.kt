package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One published day of the quote-of-the-day schedule.
 *
 * **Now holds the text itself.** Until schema version 2 of the CMS contract this row was a
 * reference — a surah and an ayat number joined against `quran_verses` at read time — so that the
 * Kemenag dataset stayed the only source of Qur'an text. The CMS now publishes free-form
 * quotations that may be hadith or neither, so there is nothing to join against and the words are
 * stored here. See [com.sangusantri.app.domain.model.AyatHariIni] for what that trade cost.
 *
 * Both translations are stored, not just the one the current device needs: the language can change
 * under a cached window, and re-syncing to follow a locale switch would be a request the app
 * otherwise never has to make.
 *
 * Keyed by [epochDay] rather than a formatted date string so the "which day is this" comparison is
 * an integer one and cannot go wrong across time zones or locales.
 */
@Entity(tableName = "ayat_hari_ini")
data class AyatHariIniEntity(
    @PrimaryKey val epochDay: Long,
    val kind: String,
    val arabic: String?,
    val translationId: String,
    val translationEn: String?,
    val sourceLabel: String,
    val sourceNote: String?,
    val theme: String?,
)
