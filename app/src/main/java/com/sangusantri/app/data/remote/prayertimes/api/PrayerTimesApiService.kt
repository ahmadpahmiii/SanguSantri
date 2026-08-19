package com.sangusantri.app.data.remote.prayertimes.api

import com.sangusantri.app.data.remote.prayertimes.dto.PrayerCityDto
import com.sangusantri.app.data.remote.prayertimes.dto.PrayerEnvelopeDto
import com.sangusantri.app.data.remote.prayertimes.dto.PrayerScheduleDto
import com.sangusantri.app.data.remote.prayertimes.dto.QiblaDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * myquran (api.myquran.com v3) — prayer schedules and qibla bearing only.
 *
 * Reached through its own unauthenticated client (`di/PrayerTimesNetworkModule.kt`), never the
 * Quran client: ADR 0016 §5 forbids sending Kemenag credentials to any non-Kemenag origin, and this
 * is a different origin entirely. Also not the CMS content client (ADR 0015).
 *
 * The service publishes Quran text, audio, tafsir and a hijri calendar too; none of that is
 * consumed here. Kemenag remains the only Quran-content API (ADR 0016 §2) and the hijri calendar
 * stays the app's own offline `java.time.chrono.HijrahDate` computation.
 */
interface PrayerTimesApiService {
    @GET("sholat/kabkota/semua")
    suspend fun getCities(): Response<PrayerEnvelopeDto<List<PrayerCityDto>>>

    /** [period] is `yyyy-MM` for a whole month (what this app asks for) or `yyyy-MM-dd` for a day. */
    @GET("sholat/jadwal/{id}/{period}")
    suspend fun getSchedule(
        @Path("id") cityId: String,
        @Path("period") period: String,
    ): Response<PrayerEnvelopeDto<PrayerScheduleDto>>

    /** [coordinate] is `lat,lon` in decimal degrees. */
    @GET("qibla/{coordinate}")
    suspend fun getQibla(
        @Path("coordinate") coordinate: String,
    ): Response<PrayerEnvelopeDto<QiblaDto>>
}
