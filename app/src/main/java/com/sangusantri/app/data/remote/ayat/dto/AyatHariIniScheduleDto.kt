package com.sangusantri.app.data.remote.ayat.dto

import kotlinx.serialization.Serializable

/**
 * The CMS's quote-of-the-day schedule (`GET /api/v1/ayat-hari-ini`), documented in
 * `../cms/docs/engineering/API.md`.
 *
 * **Schema version 2 carries the text.** Version 1 published only `surah`/`ayat` and the app
 * resolved the words from its own Kemenag dataset. The shapes are not compatible, which is exactly
 * why the version is checked before any of this is trusted — an app that meets a version it does
 * not know keeps its cache rather than guessing.
 */
@Serializable
data class AyatHariIniScheduleDto(val schemaVersion: Int, val items: List<AyatHariIniItemDto>)

@Serializable
data class AyatHariIniItemDto(
    /** ISO-8601 local date, `YYYY-MM-DD`. Compared against the device's local date, not UTC. */
    val date: String,
    /** `quran` / `hadith` / `other`. Decorative — see `QuoteKind`. */
    val kind: String? = null,
    /** Null when the quotation is not in Arabic. */
    val arabic: String? = null,
    val translation: AyatHariIniTranslationDto,
    val sourceLabel: String,
    val sourceNote: String? = null,
    val theme: String? = null,
)

@Serializable
data class AyatHariIniTranslationDto(
    /** Indonesian. Always present — it is the app's base language. */
    val id: String,
    /** English. Absent for most quotes; the app falls back to [id]. */
    val en: String? = null,
)
