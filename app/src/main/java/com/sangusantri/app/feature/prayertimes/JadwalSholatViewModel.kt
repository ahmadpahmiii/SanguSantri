package com.sangusantri.app.feature.prayertimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.prayeralarm.PrayerAlarmScheduler
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerNotificationMode
import com.sangusantri.app.domain.repository.KiblatRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Owns Jadwal Sholat + Kiblat. The countdown ticks every second here (unlike Beranda's per-minute
 * tick) because the design's countdown block reads to the second.
 *
 * On open it makes sure this month's schedule is cached; the fetch is a no-op when it already is,
 * so a returning user with no connection still sees their schedule.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JadwalSholatViewModel
@Inject
constructor(
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val kiblatRepository: KiblatRepository,
    // Called directly rather than through a use case, unlike `data/reminder/`'s scheduler: this one
    // takes no arguments and recomputes the single next alarm from whatever is currently persisted,
    // so there is no pair of writes to keep in step and calling it once too often costs nothing.
    private val prayerAlarmScheduler: PrayerAlarmScheduler,
) : ViewModel() {
    private val screenState = MutableStateFlow(ScreenState())

    private val clock: Flow<LocalTime> =
        flow {
            while (true) {
                emit(LocalTime.now())
                delay(CLOCK_TICK_MILLIS)
            }
        }

    private val cities =
        screenState
            .flatMapLatest { prayerScheduleRepository.observeCities(it.cityQuery) }

    val uiState: StateFlow<JadwalSholatUiState> =
        combine(
            prayerScheduleRepository.observeToday(),
            prayerScheduleRepository.observeSelectedCity(),
            kiblatRepository.observeDirection(),
            clock,
            combine(screenState, cities) { state, list -> state to list },
        ) { schedule, city, kiblat, now, (state, cityList) ->
            JadwalSholatUiState(
                schedule = schedule,
                selectedCity = city,
                now = now,
                today = LocalDate.now(),
                kiblat = kiblat,
                isRefreshing = state.isRefreshing,
                errorMessage = state.error,
                cityPickerVisible = state.cityPickerVisible,
                cityQuery = state.cityQuery,
                cities = cityList,
                notificationSheetPrayer = state.notificationSheetPrayer,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            JadwalSholatUiState(),
        )

    init {
        refreshSchedule()
    }

    /** Fetches this month if it is not already cached. Silent when a cached schedule already
     * covers today — an offline reopen must not turn into an error banner. */
    fun refreshSchedule() {
        viewModelScope.launch {
            screenState.update { it.copy(isRefreshing = true, error = null) }
            val result = prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
            // A newly fetched month, or a different city, changes what the next alarm is.
            prayerAlarmScheduler.rearm()
            screenState.update {
                it.copy(
                    isRefreshing = false,
                    error = if (result.isFailure) PrayerScheduleError.OFFLINE_OR_UNREACHABLE else null,
                )
            }
        }
    }

    fun openCityPicker() {
        viewModelScope.launch {
            screenState.update { it.copy(cityPickerVisible = true, isRefreshing = true, error = null) }
            val result = prayerScheduleRepository.ensureCitiesCached()
            screenState.update {
                it.copy(
                    isRefreshing = false,
                    error = if (result.isFailure) PrayerScheduleError.CITY_LIST_UNAVAILABLE else null,
                )
            }
        }
    }

    fun dismissCityPicker() {
        screenState.update { it.copy(cityPickerVisible = false, cityQuery = "") }
    }

    fun updateCityQuery(query: String) {
        screenState.update { it.copy(cityQuery = query) }
    }

    fun selectCity(cityId: String) {
        viewModelScope.launch {
            prayerScheduleRepository.selectCity(cityId)
            screenState.update { it.copy(cityPickerVisible = false, cityQuery = "", isRefreshing = true) }
            val result = prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
            // A newly fetched month, or a different city, changes what the next alarm is.
            prayerAlarmScheduler.rearm()
            screenState.update {
                it.copy(
                    isRefreshing = false,
                    error = if (result.isFailure) PrayerScheduleError.OFFLINE_OR_UNREACHABLE else null,
                )
            }
        }
    }

    /** Called once the location permission has been granted; a denial leaves the compass as it was. */
    fun refreshKiblat() {
        viewModelScope.launch {
            screenState.update { it.copy(isRefreshing = true, error = null) }
            val result = kiblatRepository.refreshDirection()
            screenState.update {
                it.copy(
                    isRefreshing = false,
                    error = if (result.isFailure) PrayerScheduleError.LOCATION_UNAVAILABLE else null,
                )
            }
        }
    }

    /** Fills the city in from the device's coarse position. Falls back to an explicit message —
     * never a guessed city — when nothing matches. */
    fun detectCity() {
        viewModelScope.launch {
            screenState.update { it.copy(isRefreshing = true, error = null) }
            when (val detected = prayerScheduleRepository.detectAndSelectCity()) {
                is CityDetection.Detected -> {
                    prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
                    prayerAlarmScheduler.rearm()
                    screenState.update { it.copy(isRefreshing = false, error = null) }
                }

                // Resolved a place but not a single city — open the picker already filtered to it,
                // so confirming is one tap rather than a search from scratch.
                is CityDetection.Ambiguous ->
                    screenState.update {
                        it.copy(
                            isRefreshing = false,
                            error = null,
                            cityPickerVisible = true,
                            cityQuery = detected.query,
                        )
                    }

                CityDetection.Unavailable ->
                    screenState.update {
                        it.copy(isRefreshing = false, error = PrayerScheduleError.CITY_DETECTION_FAILED)
                    }
            }
            prayerScheduleRepository.ensureCitiesCached()
        }
    }

    fun openNotificationSheet(prayer: PrayerName) {
        screenState.update { it.copy(notificationSheetPrayer = prayer) }
    }

    fun dismissNotificationSheet() {
        screenState.update { it.copy(notificationSheetPrayer = null) }
    }

    fun setNotificationMode(
        prayer: PrayerName,
        mode: PrayerNotificationMode,
    ) {
        screenState.update { it.copy(notificationSheetPrayer = null) }
        viewModelScope.launch {
            prayerScheduleRepository.setNotificationMode(prayer, mode)
            prayerAlarmScheduler.rearm()
        }
    }

    private data class ScreenState(
        val cityPickerVisible: Boolean = false,
        val notificationSheetPrayer: PrayerName? = null,
        val cityQuery: String = "",
        val isRefreshing: Boolean = false,
        val error: PrayerScheduleError? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val CLOCK_TICK_MILLIS = 1_000L
    }
}
