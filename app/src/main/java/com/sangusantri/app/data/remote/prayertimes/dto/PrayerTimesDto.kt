package com.sangusantri.app.data.remote.prayertimes.dto

import kotlinx.serialization.Serializable

/**
 * myquran wraps every response in `{status, message, data}` (api.myquran.com v3). One envelope type
 * for all of them, mirroring `QuranEnvelopeDto`'s shape for the Kemenag API.
 */
@Serializable
data class PrayerEnvelopeDto<T>(
    val status: Boolean,
    val message: String = "",
    val data: T? = null,
)

/** `GET /sholat/kabkota/semua` and `/sholat/kabkota/cari/{keyword}` item. */
@Serializable
data class PrayerCityDto(
    val id: String,
    val lokasi: String,
)

/** `GET /sholat/jadwal/{id}/{period}` payload. [jadwal] is keyed by ISO date, one entry per day of
 * the requested month. */
@Serializable
data class PrayerScheduleDto(
    val id: String,
    val kabko: String,
    val prov: String,
    val jadwal: Map<String, PrayerScheduleEntryDto>,
)

/**
 * One published day. Every time is an `HH:mm` string and is carried through to Room unparsed —
 * this app never recomputes or adjusts a published prayer time.
 *
 * `tanggal` (the API's pre-formatted Indonesian date string) is deliberately not decoded: the app
 * formats dates itself from the ISO key, so it has one date-formatting path, not two.
 */
@Serializable
data class PrayerScheduleEntryDto(
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String,
)

/** `GET /qibla/{lat},{lon}` payload. [direction] is degrees clockwise from true north. */
@Serializable
data class QiblaDto(
    val latitude: Double,
    val longitude: Double,
    val direction: Double,
)
