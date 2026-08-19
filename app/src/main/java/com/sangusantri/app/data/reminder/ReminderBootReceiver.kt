package com.sangusantri.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sangusantri.app.data.prayeralarm.PrayerAlarmScheduler
import com.sangusantri.app.domain.usecase.RescheduleAllRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `AlarmManager` alarms are cleared on every reboot — ROADMAP.md's "Rescheduling after reboot"
 * requirement means every enabled reminder must be re-armed here, not just left silently
 * unscheduled until the user happens to reopen the app.
 *
 * The prayer alarm is re-armed from the same broadcast rather than from a second receiver: it is the
 * same one-line consequence of the same lost alarms, and a `BOOT_COMPLETED` receiver whose only
 * difference is which scheduler it calls would be a file for nothing.
 */
@AndroidEntryPoint
class ReminderBootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var rescheduleAllReminders: RescheduleAllRemindersUseCase

    @Inject
    lateinit var prayerAlarmScheduler: PrayerAlarmScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleAllReminders()
                prayerAlarmScheduler.rearm()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
