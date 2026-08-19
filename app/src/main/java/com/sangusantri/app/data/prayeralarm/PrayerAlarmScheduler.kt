package com.sangusantri.app.data.prayeralarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps exactly one system alarm armed: the next prayer announcement that is due.
 *
 * One alarm rather than six, re-armed by [PrayerAlarmReceiver] each time one fires, so the chain
 * sustains itself without the app being opened. [rearm] is idempotent — call it after anything that
 * can change the answer (a mode change, a city change, a reboot, app start).
 *
 * **Exact, unlike everything else in this app that schedules.** `data/reminder/` and the home-screen
 * widget both use inexact alarms and say why; an adzan cannot. `setAndAllowWhileIdle` may hold an
 * alarm for several minutes in Doze, and an adzan minutes after the published time is not a late
 * reminder, it is a wrong one. That is what `USE_EXACT_ALARM` in the manifest is declared for.
 */
@Singleton
class PrayerAlarmScheduler
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: PrayerScheduleRepository,
) {
    private val alarmManager: AlarmManager? = context.getSystemService()

    suspend fun rearm() {
        val manager = alarmManager ?: return
        // Half a minute of slack so re-arming from the receiver cannot pick the event that just
        // fired and loop on it.
        val from = LocalDateTime.now().plusSeconds(REARM_SLACK_SECONDS)
        val today = LocalDate.now()
        val next =
            PrayerAlarmPlan.nextEventAfter(
                from = from,
                today = repository.scheduleOn(today),
                tomorrow = tomorrowSchedule(today.plusDays(1)),
            )
        if (next == null) {
            manager.cancel(pendingIntent(PrayerAlarmReceiver.intent(context, null)))
            return
        }
        val triggerAt =
            next.at
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        val pendingIntent = pendingIntent(PrayerAlarmReceiver.intent(context, next))
        if (canScheduleExact()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            // Only reachable if the user revokes the alarm permission in system settings. Late is
            // still better than never.
            Log.w(TAG, "exact alarms unavailable; prayer alarm armed inexactly")
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    /** Tomorrow, fetching the month first if the calendar has just rolled over into one that was
     * never cached — otherwise the chain would go quiet on the 1st until the app is next opened. */
    private suspend fun tomorrowSchedule(tomorrow: LocalDate) =
        repository.scheduleOn(tomorrow) ?: run {
            repository.ensureScheduleCached(tomorrow)
            repository.scheduleOn(tomorrow)
        }

    private fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager?.canScheduleExactAlarms() == true

    /** One request code for the app's single prayer alarm; `FLAG_UPDATE_CURRENT` replaces the extras
     * of the previously armed one rather than stacking a second alarm beside it. */
    private fun pendingIntent(intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val TAG = "PrayerAlarm"
        const val REQUEST_CODE = 0x5A1A
        const val REARM_SLACK_SECONDS = 30L
    }
}
