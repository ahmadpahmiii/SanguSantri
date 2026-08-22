package com.sangusantri.app.data.remote.ayat.dto

import kotlinx.serialization.Serializable

/**
 * The CMS's ayat-of-the-day schedule (`GET /api/v1/ayat-hari-ini`), documented in
 * `docs/product/AYAT_HARI_INI_CMS_BRIEF.md`.
 *
 * Note what is absent: no Arabic, no translation, no surah name. The endpoint publishes editorial
 * decisions, and the app resolves each one against its own Kemenag dataset. That is deliberate —
 * it keeps the payload tiny, keeps Kemenag the single source of Quran text, and means a CMS bug can
 * at worst schedule the wrong ayat, never a corrupted one.
 */
@Serializable
data class AyatHariIniScheduleDto(
    val schemaVersion: Int,
    val items: List<AyatHariIniItemDto>,
)

@Serializable
data class AyatHariIniItemDto(
    /** ISO-8601 local date, `YYYY-MM-DD`. */
    val date: String,
    val surah: Int,
    val ayat: Int,
    val theme: String? = null,
)
