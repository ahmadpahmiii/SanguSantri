package com.sangusantri.app.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.sangusantri.app.data.prayeralarm.PrayerAlarmScheduler
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.repository.KiblatRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
 * **Never asks for `ACCESS_COARSE_LOCATION`; it no-ops without it.** The check lives here rather
 * than at each call site because it is a precondition of the whole sequence, and because neither
 * caller — a cold start, a widget tap — is an occasion to raise a permission dialog. Location stays
 * optional and on-demand (`docs/security/PRIVACY.md`); Jadwal Sholat's "Izinkan lokasi" is still
 * the one place that asks.
 *
 * A guessed city is never selected: that rule lives in
 * [PrayerScheduleRepository.detectAndSelectCity] and is unchanged here — an ambiguous or unmatched
 * place leaves whatever city the reader already had.
 *
 * A failed bearing is deliberately silent. The cached one stays correct to well under a degree
 * across a city, and this refresh is automatic — it must not push an error over a city detection
 * that just succeeded.
 */
class RefreshFromCurrentLocationUseCase
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val prayerScheduleRepository: PrayerScheduleRepository,
    private val kiblatRepository: KiblatRepository,
    private val prayerAlarmScheduler: PrayerAlarmScheduler,
) {
    suspend operator fun invoke() {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val detected = prayerScheduleRepository.detectAndSelectCity()
        if (detected is CityDetection.Detected) {
            prayerScheduleRepository.ensureScheduleCached(LocalDate.now())
            // A different city, or a newly fetched month, changes what the next adzan is.
            prayerAlarmScheduler.rearm()
        }
        kiblatRepository.refreshDirection()
        prayerScheduleRepository.ensureCitiesCached()
    }
}
