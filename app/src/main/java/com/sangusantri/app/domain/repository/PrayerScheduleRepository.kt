package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.model.PrayerCity
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerSchedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Jadwal Sholat's data, from myquran (api.myquran.com v3) with Room as the source of truth.
 *
 * The schedule is keyed by the user's chosen kabupaten/kota, not by coordinates — which is why
 * prayer times need no location permission. [observeToday] emits `null` until a city is chosen or
 * while the first fetch for it is still pending; Beranda and Jadwal Sholat render nothing rather
 * than a guessed schedule.
 *
 * A whole month is fetched per call and cached, so the schedule keeps working offline for the rest
 * of the month.
 */
interface PrayerScheduleRepository {
    fun observeToday(): Flow<PrayerSchedule?>

    /** `null` until the user picks one. */
    fun observeSelectedCity(): Flow<PrayerCity?>

    /** Whole list when [query] is blank, filtered otherwise. Reads Room; [ensureCitiesCached]
     * fills it. */
    fun observeCities(query: String): Flow<List<PrayerCity>>

    /** Fetches the ~517-city list on first use. No-op once cached. */
    suspend fun ensureCitiesCached(): Result<Unit>

    suspend fun selectCity(cityId: String)

    /**
     * Matches the device's coarse position to a kabupaten/kota and selects it.
     *
     * Optional convenience only — the manual picker is always available and is the sole path when
     * location is denied. Returns [CityDetection.Ambiguous] rather than a guess when the resolved
     * place name does not pin down exactly one city; a wrong city means wrong prayer times.
     */
    suspend fun detectAndSelectCity(): CityDetection

    /** `true` once the app has asked for location at least once, so first launch asks and later
     * launches do not nag. */
    fun observeLocationPromptShown(): Flow<Boolean>

    suspend fun markLocationPromptShown()

    /** Fetches [month]'s schedule for the selected city if it is not already cached. */
    suspend fun ensureScheduleCached(month: LocalDate): Result<Unit>

    /** Per-prayer reminder flags — real user state, independent of where the times come from. */
    suspend fun setNotificationEnabled(
        prayer: PrayerName,
        enabled: Boolean,
    )
}
