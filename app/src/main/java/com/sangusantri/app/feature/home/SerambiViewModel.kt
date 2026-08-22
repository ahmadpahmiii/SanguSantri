package com.sangusantri.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Builds Beranda exclusively from local repositories; Room remains the source of truth. */
@HiltViewModel
class SerambiViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
    reminderRepository: ReminderRepository,
    nahwuQuizRepository: NahwuQuizRepository,
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val settingsRepository: QuranReaderSettingsRepository,
    private val resumeCoordinator: SerambiResumeCoordinator,
) : ViewModel() {
    // Sholawat (0.0.8) deliberately has its own list + reader (feature/sholawat), not the
    // Full/Guided Amaliyah reader Beranda's featured section and resume widget route through — so
    // it must never appear in `activeContent`, only be counted for `hasSholawatContent`'s gate.
    private val rawActiveContent = contentRepository.observeActiveContent()
    private val activeContent =
        rawActiveContent.map { items -> items.filterNot { it.isSholawat } }

    private val baseData: Flow<BaseData> =
        combine(
            activeContent,
            rawActiveContent.map { items -> items.any { it.isSholawat } },
            reminderRepository.observeNearestEnabled(),
            nahwuQuizRepository.observePackageSummaries().map { it.isNotEmpty() },
            nahwuQuizRepository.observeActiveAttempt(),
        ) { items, hasSholawatContent, nearestReminder, hasNahwuQuizContent, activeQuiz ->
            BaseData(items, hasSholawatContent, nearestReminder, hasNahwuQuizContent, activeQuiz != null)
        }

    /** One tick a minute is all the next-prayer block's countdown, position line, and highlighted
     * row need; the second-precision countdown belongs to Jadwal Sholat, not Beranda. */
    private val clock: Flow<LocalTime> =
        flow {
            while (true) {
                emit(LocalTime.now())
                delay(CLOCK_TICK_MILLIS)
            }
        }

    val uiState: StateFlow<SerambiUiState> =
        combine(
            baseData,
            resumeCoordinator.observe(activeContent),
            prayerScheduleRepository.observeToday(),
            clock,
        ) { base, resumeItem, prayerSchedule: PrayerSchedule?, now ->
            SerambiUiState.Loaded(
                items = base.items,
                nearestReminder = base.nearestReminder,
                hasNahwuQuizContent = base.hasNahwuQuizContent,
                hasActiveNahwuQuiz = base.hasActiveNahwuQuiz,
                hasSholawatContent = base.hasSholawatContent,
                resumeItem = resumeItem,
                prayerSchedule = prayerSchedule,
                now = now,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SerambiUiState.Loading,
        )

    fun dismissResume(fingerprint: String) {
        viewModelScope.launch { resumeCoordinator.dismiss(fingerprint) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    /** Emits once, on the very first launch, so Beranda can ask for location to set the prayer
     * schedule up. Never re-emits — a denied permission must not turn into a prompt every launch. */
    val shouldAskForLocation: StateFlow<Boolean> =
        combine(
            prayerScheduleRepository.observeLocationPromptShown(),
            prayerScheduleRepository.observeSelectedCity(),
        ) { alreadyAsked, city -> !alreadyAsked && city == null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    private val _detectingCity = MutableStateFlow(false)

    /** True while the position is being resolved to a city, so Beranda can say it is looking rather
     * than flashing "pilih kota" at someone who just granted permission. */
    val detectingCity: StateFlow<Boolean> = _detectingCity.asStateFlow()

    /**
     * Called once the permission dialog has been answered, either way.
     *
     * A grant should mean the reader never sees "pilih kota" at all — detection runs immediately and
     * the schedule follows. It can still fail (no fix, geocoder unreachable, a position outside
     * Indonesia), and the manual picker stays as the fallback rather than a guessed city.
     */
    fun onLocationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            prayerScheduleRepository.markLocationPromptShown()
            if (!granted) return@launch
            _detectingCity.value = true
            val detected = prayerScheduleRepository.detectAndSelectCity()
            if (detected is CityDetection.Detected) prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
            _detectingCity.value = false
        }
    }

    private data class BaseData(
        val items: List<Content>,
        val hasSholawatContent: Boolean,
        val nearestReminder: Reminder?,
        val hasNahwuQuizContent: Boolean,
        val hasActiveNahwuQuiz: Boolean,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val CLOCK_TICK_MILLIS = 60_000L
    }
}
