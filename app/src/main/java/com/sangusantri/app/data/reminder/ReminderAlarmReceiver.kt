package com.sangusantri.app.data.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sangusantri.app.MainActivity
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.ReminderSchedule
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import com.sangusantri.app.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when a reminder's [ReminderAlarmScheduler] alarm goes off: shows the notification, then
 * re-arms the next occurrence (or disables a one-off [ReminderSchedule.HijriDate] reminder rather
 * than deleting it — same "preserve, don't delete" pattern as completed Tasbih/Guided Reader
 * state). `AlarmManager` alarms do not survive a reboot; `ReminderBootReceiver` handles that
 * separately.
 *
 * `onReceive` cannot suspend directly, so the Room read + alarm re-arm run inside `goAsync()` — the
 * standard pattern for a `BroadcastReceiver` that needs to do async work before the system may kill
 * the process.
 */
@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var contentRepository: ContentRepository

    @Inject
    lateinit var scheduleReminder: ScheduleReminderUseCase

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleAlarm(context, reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAlarm(
        context: Context,
        reminderId: String,
    ) {
        val reminder = reminderRepository.getById(reminderId) ?: return
        if (!reminder.isEnabled) return

        val contentTitle = contentRepository.getContentById(reminder.contentId)?.title
        showNotification(context, reminder, contentTitle)
        rearmOrDisable(reminder)
    }

    private fun showNotification(
        context: Context,
        reminder: Reminder,
        contentTitle: String?,
    ) {
        val tapIntent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_REMINDER_CONTENT_ID, reminder.contentId)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val tapPendingIntent =
            PendingIntent.getActivity(
                context,
                reminder.id.hashCode(),
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, ReminderNotificationChannel.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(reminder.label.ifBlank { contentTitle ?: context.getString(R.string.app_name) })
                .setContentText(
                    contentTitle?.let { context.getString(R.string.reminder_notification_body, it) },
                ).setContentIntent(tapPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        // Explicit check (not just relying on NotificationManagerCompat's internal safe no-op) so
        // static analysis can verify this call, and so the missing-permission case is visible in
        // source. The alarm still fires and rearms below regardless of the outcome here
        // (docs/design's "no remind me later" — nothing to retry, the next scheduled occurrence is
        // the natural retry).
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(reminder.id.hashCode(), notification)
        }
    }

    private suspend fun rearmOrDisable(reminder: Reminder) {
        val schedule = reminder.schedule
        val shouldDisableAfterFiring = schedule is ReminderSchedule.HijriDate && !schedule.repeatsYearly
        scheduleReminder(reminder.copy(isEnabled = !shouldDisableAfterFiring))
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
