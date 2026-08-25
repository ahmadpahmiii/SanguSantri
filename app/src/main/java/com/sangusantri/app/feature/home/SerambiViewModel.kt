package com.sangusantri.app.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.sync.ContentSyncManager
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.repository.AyatHariIniRepository
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.KiblatRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import com.sangusantri.app.domain.usecase.ObserveAmalanHarianUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Builds Beranda exclusively from local repositories; Room remains the source of truth.
 *
 * `LongParameterList` is suppressed because Beranda is the one screen that shows something from
 * nearly every part of the app — content, reminders, quiz, prayer times, the Quran dataset, the
 * qibla bearing, settings — and each of those is a repository it must read. Grouping them behind a
 * façade would add an indirection that exists only to satisfy a count.
 */
@Suppress("LongParameterList")
@HiltViewModel
class SerambiViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
    reminderRepository: ReminderRepository,
    nahwuQuizRepository: NahwuQuizRepository,
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val settingsRepository: QuranReaderSettingsRepository,
    private val ayatHariIniRepository: AyatHariIniRepository,
    kiblatRepository: KiblatRepository,
    private val resumeCoordinator: SerambiResumeCoordinator,
    private val contentSyncManager: ContentSyncManager,
    observeAmalanHarian: ObserveAmalanHarianUseCase,
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

    /**
     * Re-reads the cached schedule after a sync has actually written one.
     *
     * Without this the header is read once per date and never again, so on a device whose
     * `ayat_hari_ini` table is empty when Beranda first composes — a fresh install, or any install
     * that has just taken the destructive schema migration — the read returns null, the sync
     * finishes a moment later, and nothing asks Room again until the midnight rollover. The reader
     * would see no quote for their entire first day.
     */
    private val ayatRefresh = MutableStateFlow(0)

    /**
     * The page header's values: the ayat of the day, the qibla bearing, and the Arabic typeface the
     * ayah renders in.
     *
     * The ayat rides the same minute tick as the countdown rather than a ticker of its own —
     * `distinctUntilChanged` on the *date* means Room is read once a day, at the midnight rollover,
     * not once a minute. [ayatRefresh] adds the one other moment worth re-reading at: immediately
     * after [refreshAyatHariIni] has fetched a window.
     */
    private val headerData: Flow<HeaderData> =
        combine(
            combine(
                clock.map { LocalDate.now() }.distinctUntilChanged(),
                ayatRefresh,
            ) { date, _ -> date }.map { ayatHariIniRepository.forDate(it) },
            kiblatRepository.observeDirection().map { it?.bearingDegrees },
            settingsRepository.observe().map { it.arabicFont }.distinctUntilChanged(),
            // Rides along with the header rather than taking a sixth slot in the state combine
            // below: the streak pill is part of the header band, above the prayer block.
            observeAmalanHarian(),
        ) { ayat, bearing, arabicFont, amalan -> HeaderData(ayat, bearing, arabicFont, amalan) }

    val uiState: StateFlow<SerambiUiState> =
        combine(
            baseData,
            resumeCoordinator.observe(activeContent),
            prayerScheduleRepository.observeToday(),
            clock,
            headerData,
        ) { base, resumeItem, prayerSchedule: PrayerSchedule?, now, header ->
            SerambiUiState.Loaded(
                items = base.items,
                nearestReminder = base.nearestReminder,
                hasNahwuQuizContent = base.hasNahwuQuizContent,
                hasActiveNahwuQuiz = base.hasActiveNahwuQuiz,
                hasSholawatContent = base.hasSholawatContent,
                resumeItem = resumeItem,
                prayerSchedule = prayerSchedule,
                now = now,
                ayatHariIni = header.ayatHariIni,
                kiblatBearingDegrees = header.kiblatBearingDegrees,
                arabicFont = header.arabicFont,
                amalan = header.amalan,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SerambiUiState.Loading,
        )

    init {
        refresh()
    }

    /**
     * Everything Beranda pulls from the CMS, in one call, safe to run on every resume.
     *
     * Called from `Lifecycle.Event.ON_RESUME` rather than only from [init]: a ViewModel survives
     * backgrounding, so init alone means a reader who leaves the app open for a week never sees
     * anything published in it. Resume is the moment they are actually looking.
     *
     * The cost of doing this often is what the `schemaVersion` 3 split bought. The two category
     * listings are a few hundred bytes each and carry no steps, so their ETags do not move when
     * someone corrects a word inside an item — the common resume is two `304`s and no body.
     * Offline it is two failed requests that change nothing; Room has already drawn the screen.
     */
    fun refresh() {
        refreshContentCatalogue()
        refreshAyatHariIni()
    }

    /**
     * Pulls the published catalogue so a publish or unpublish in the CMS lands here without
     * waiting for the background sync window.
     *
     * Failures are silent by design: Beranda has already rendered from Room, a stale catalogue
     * is exactly what an offline reader wants, and there is nothing for them to act on.
     */
    private fun refreshContentCatalogue() {
        viewModelScope.launch {
            runCatching { contentSyncManager.sync() }
                .onFailure { Log.w(TAG, "Beranda content refresh failed", it) }
        }
    }

    /**
     * Asks the CMS for the published schedule, once per Beranda.
     *
     * Offline-first: this is a refresh, not a load. Beranda has already rendered from Room by the
     * time it returns, and a failure is silent by design — the reader keeps the cached schedule and
     * there is nothing for them to act on. The request itself is skipped entirely when Room already
     * holds today's entry, so the common case costs no network at all.
     */
    private fun refreshAyatHariIni() {
        viewModelScope.launch {
            runCatching { ayatHariIniRepository.sync() }
                .onFailure { Log.w(TAG, "Beranda ayat refresh failed", it) }
            // Re-read regardless of the result. A sync that no-ops because today was already
            // cached changes nothing, and a failed one leaves the cache untouched, so the extra
            // read is cheap in both cases and is the only thing that makes a first successful
            // fetch visible before tomorrow.
            ayatRefresh.update { it + 1 }
        }
    }

    fun dismissResume(fingerprint: String) {
        viewModelScope.launch { resumeCoordinator.dismiss(fingerprint) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    /** Emits once, on the very first launch, so Beranda can ask for location & notification permissions
     * to set the prayer schedule & adzan notifications up. Never re-emits — a denied permission
     * transitions to the subsequent launch bottom sheet check. */
    val shouldAskForInitialPermissions: StateFlow<Boolean> =
        combine(
            prayerScheduleRepository.observeLocationPromptShown(),
            prayerScheduleRepository.observeSelectedCity(),
        ) { alreadyAsked, city -> !alreadyAsked && city == null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    /** Backward compatibility alias for [shouldAskForInitialPermissions]. */
    val shouldAskForLocation: StateFlow<Boolean> get() = shouldAskForInitialPermissions

    /** Whether the initial permission prompt has already been shown to the user on a previous session. */
    val hasInitialPromptBeenShown: StateFlow<Boolean> =
        prayerScheduleRepository
            .observeLocationPromptShown()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    private val _detectingCity = MutableStateFlow(false)

    /** True while the position is being resolved to a city, so Beranda can say it is looking rather
     * than flashing "pilih kota" at someone who just granted permission. */
    val detectingCity: StateFlow<Boolean> = _detectingCity.asStateFlow()

    /**
     * Called once the initial permissions prompt has been answered.
     *
     * Marks the prompt as shown. If location was granted, runs city detection and caches the schedule.
     */
    fun onInitialPermissionsResult(locationGranted: Boolean) {
        viewModelScope.launch {
            prayerScheduleRepository.markLocationPromptShown()
            if (!locationGranted) return@launch
            triggerCityDetection()
        }
    }

    /** Backward compatibility alias for [onInitialPermissionsResult]. */
    fun onLocationPermissionResult(granted: Boolean) = onInitialPermissionsResult(granted)

    /**
     * Called when location permission is granted subsequently (e.g. via PermissionBottomSheet or App Settings).
     */
    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            triggerCityDetection()
        }
    }

    private suspend fun triggerCityDetection() {
        _detectingCity.value = true
        val detected = prayerScheduleRepository.detectAndSelectCity()
        if (detected is CityDetection.Detected) prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
        _detectingCity.value = false
    }

    private data class HeaderData(
        val ayatHariIni: AyatHariIni?,
        val kiblatBearingDegrees: Float?,
        val arabicFont: QuranArabicFont,
        val amalan: AmalanHarian,
    )

    private data class BaseData(
        val items: List<Content>,
        val hasSholawatContent: Boolean,
        val nearestReminder: Reminder?,
        val hasNahwuQuizContent: Boolean,
        val hasActiveNahwuQuiz: Boolean,
    )

    private companion object {
        const val TAG = "SerambiViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val CLOCK_TICK_MILLIS = 60_000L
    }
}
