package com.sangusantri.app.domain.usecase

import com.sangusantri.app.data.prayeralarm.PrayerAlarmScheduler
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.repository.KiblatRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Re-derives everything that depends on where the device actually is: the kabupaten/kota the
 * schedule is keyed by, this month's times for it, the next adzan alarm, and the qibla bearing.
 *
 * A use case rather than a method on one ViewModel because more than one surface needs the whole
 * sequence — the home-screen widget's tap raises it above any single screen, and both Beranda and
 * Jadwal Sholat render the result. Neither of those has to ask for it: both read the same Room
 * flows, so whichever one is on screen re-renders as soon as this writes.
 *
 * **Callers must have checked that `ACCESS_COARSE_LOCATION` is already granted — this never asks.**
 * A widget tap is not consent, and location stays optional and on-demand
 * (`docs/security/PRIVACY.md`).
 *
 * Returns the [CityDetection] so a caller that has UI for it can react — Jadwal Sholat opens the
 * city picker pre-filtered on [CityDetection.Ambiguous]. A guessed city is never selected: that
 * rule lives in [PrayerScheduleRepository.detectAndSelectCity] and is unchanged here.
 *
 * A failed bearing is deliberately silent. The cached one stays correct to well under a degree
 * across a city, and this refresh is automatic — it must not push an error over a city detection
 * that just succeeded.
 */
class RefreshFromCurrentLocationUseCase
@Inject
constructor(
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val kiblatRepository: KiblatRepository,
    private val prayerAlarmScheduler: PrayerAlarmScheduler,
) {
    suspend operator fun invoke(): CityDetection {
        val detected = prayerScheduleRepository.detectAndSelectCity()
        if (detected is CityDetection.Detected) {
            prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
            // A different city, or a newly fetched month, changes what the next adzan is.
            prayerAlarmScheduler.rearm()
        }
        kiblatRepository.refreshDirection()
        prayerScheduleRepository.ensureCitiesCached()
        return detected
    }
}
