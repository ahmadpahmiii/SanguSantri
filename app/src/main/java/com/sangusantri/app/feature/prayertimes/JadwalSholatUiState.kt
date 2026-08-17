package com.sangusantri.app.feature.prayertimes

import com.sangusantri.app.domain.model.PrayerCity
import com.sangusantri.app.domain.model.PrayerSchedule
import java.time.LocalDate
import java.time.LocalTime

/**
 * Jadwal Sholat + Kiblat state.
 *
 * [schedule] is `null` until a city is chosen and its month has been fetched — the screen then
 * prompts for a city (or reports the fetch failure) instead of showing an invented schedule.
 *
 * [kiblatBearing] is `null` until the user grants location and a bearing has been computed; the
 * compass draws no needle without one.
 */
data class JadwalSholatUiState(
    val schedule: PrayerSchedule? = null,
    val selectedCity: PrayerCity? = null,
    val now: LocalTime = LocalTime.MIDNIGHT,
    val today: LocalDate = LocalDate.now(),
    val kiblatBearing: Float? = null,
    val isRefreshing: Boolean = false,
    /** Set when a fetch failed and nothing usable is cached — the difference between "no city yet"
     * and "we tried and could not reach the source" matters to the person reading the screen. */
    val errorMessage: PrayerScheduleError? = null,
    val cityPickerVisible: Boolean = false,
    val cityQuery: String = "",
    val cities: List<PrayerCity> = emptyList(),
)

enum class PrayerScheduleError {
    OFFLINE_OR_UNREACHABLE,
    CITY_LIST_UNAVAILABLE,
    LOCATION_UNAVAILABLE,
    CITY_DETECTION_FAILED,
}
