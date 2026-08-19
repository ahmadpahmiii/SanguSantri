package com.sangusantri.app.data.prayeralarm

import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerNotificationMode
import com.sangusantri.app.domain.model.PrayerSchedule
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/** The bundled recordings, by role rather than by file — the mapping to `res/raw` lives in
 * [AdzanPlaybackService], which is the only thing that ever opens one. */
enum class AdzanTrack {
    TARHIM,
    ADZAN_SUBUH,
    ADZAN,
}

/**
 * One thing to do at one moment: announce [prayer] at [at], by playing [tracks] back to back, or —
 * when [tracks] is empty — by posting an ordinary notification and letting the device's own profile
 * decide how loud that is.
 */
data class PrayerAlarmEvent(
    val at: LocalDateTime,
    val prayer: PrayerName,
    val tracks: List<AdzanTrack>,
)

/**
 * Turns a day's schedule plus the reader's per-row settings into the moments an alarm must fire.
 *
 * Pure and side-effect free on purpose: [PrayerAlarmScheduler] arms what this produces and
 * [PrayerAlarmReceiver] plays it, so the product's rules live in exactly one place.
 *
 * The two rules that are not "announce each prayer at its own time":
 * - **Imsak with adzan** plays the tarhim twice, back to back. Imsak is not a prayer and has no
 *   adzan of its own.
 * - **Imsak off, Subuh with adzan** moves the tarhim to just before Subuh instead — it fires
 *   [TARHIM_LEAD] early so the tarhim ends, and the adzan begins, on the published Subuh minute.
 */
object PrayerAlarmPlan {
    /** The bundled tarhim's own running time (4m55s), so the adzan that follows it lands on time. */
    val TARHIM_LEAD: Duration = Duration.ofSeconds(295)

    /** Every alarm [schedule] calls for on [date], earliest first. */
    fun eventsFor(
        schedule: PrayerSchedule,
        date: LocalDate,
    ): List<PrayerAlarmEvent> {
        val imsakOff =
            schedule.times.firstOrNull { it.name == PrayerName.IMSAK }?.notificationMode ==
                PrayerNotificationMode.NONAKTIF
        return schedule.times
            .filter { it.notificationMode != PrayerNotificationMode.NONAKTIF }
            .map { prayer ->
                val leadsWithTarhim =
                    prayer.name == PrayerName.SUBUH &&
                        prayer.notificationMode == PrayerNotificationMode.ADZAN &&
                        imsakOff
                PrayerAlarmEvent(
                    at =
                        LocalDateTime.of(date, prayer.time).let {
                            if (leadsWithTarhim) it.minus(TARHIM_LEAD) else it
                        },
                    prayer = prayer.name,
                    tracks =
                        when {
                            prayer.notificationMode == PrayerNotificationMode.BAWAAN -> emptyList()
                            leadsWithTarhim -> listOf(AdzanTrack.TARHIM, AdzanTrack.ADZAN_SUBUH)
                            prayer.name == PrayerName.IMSAK -> listOf(AdzanTrack.TARHIM, AdzanTrack.TARHIM)
                            prayer.name == PrayerName.SUBUH -> listOf(AdzanTrack.ADZAN_SUBUH)
                            else -> listOf(AdzanTrack.ADZAN)
                        },
                )
            }.sortedBy { it.at }
    }

    /**
     * The first event strictly after [from], looking into [tomorrow] once [today] is spent — after
     * Isya the next thing due really is tomorrow morning, and an alarm has to be armed for it
     * tonight rather than whenever the app is next opened.
     */
    fun nextEventAfter(
        from: LocalDateTime,
        today: PrayerSchedule?,
        tomorrow: PrayerSchedule?,
    ): PrayerAlarmEvent? {
        val date = from.toLocalDate()
        val events =
            listOfNotNull(
                today?.let { eventsFor(it, date) },
                tomorrow?.let { eventsFor(it, date.plusDays(1)) },
            ).flatten()
        return events.firstOrNull { it.at.isAfter(from) }
    }
}
