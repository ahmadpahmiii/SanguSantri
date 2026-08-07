package com.sangusantri.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.sangusantri.app.domain.model.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Wraps [AlarmManager] for one [Reminder] at a time — never called directly from a ViewModel or
 * `BroadcastReceiver`, only from the `domain/usecase/` orchestrators that also persist Room state
 * (`ScheduleReminderUseCase`, `CancelReminderUseCase`, `RescheduleAllRemindersUseCase`), so the two
 * never drift apart.
 *
 * Deliberately inexact (`setAndAllowWhileIdle`, not `setExactAndAllowWhileIdle`) — a few minutes of
 * tolerance is acceptable for a reading reminder, and avoids `SCHEDULE_EXACT_ALARM`, which Google
 * Play restricts to alarm-clock/calendar-class apps and this is neither.
 */
class ReminderAlarmScheduler
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager? = context.getSystemService()

    fun scheduleAlarm(reminder: Reminder) {
        val pendingIntent = alarmPendingIntent(reminder.id)
        alarmManager?.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.nextTriggerAtEpochMillis,
            pendingIntent,
        )
    }

    fun cancelAlarm(reminderId: String) {
        val pendingIntent = alarmPendingIntent(reminderId)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun alarmPendingIntent(reminderId: String): PendingIntent {
        val intent =
            Intent(context, ReminderAlarmReceiver::class.java).apply {
                putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            }
        return PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
