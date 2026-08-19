package com.sangusantri.app.data.prayeralarm

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
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.feature.home.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires at a prayer's moment: announces it, then arms the next one.
 *
 * What to announce travels in the intent rather than being recomputed here. The alarm is armed
 * hours ahead at most and re-armed after every firing, and a reboot clears it and re-arms from
 * scratch, so the extras cannot go stale — while recomputing would mean deciding, at 04:04, which
 * of the day's events "this" firing was meant to be.
 */
@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var scheduler: PrayerAlarmScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val prayer = PrayerName.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_PRAYER) }
        val tracks =
            intent.getStringArrayExtra(EXTRA_TRACKS).orEmpty().mapNotNull { name ->
                AdzanTrack.entries.firstOrNull { it.name == name }
            }
        if (prayer != null) {
            if (tracks.isEmpty()) {
                showNotification(context, prayer)
            } else {
                // Started from an exact alarm, which is what exempts this from the background
                // foreground-service start restriction.
                ContextCompat.startForegroundService(
                    context,
                    AdzanPlaybackService.intent(context, prayer, tracks),
                )
            }
        }
        // Always, even when the extras were unreadable: the chain must not end here.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduler.rearm()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        prayer: PrayerName,
    ) {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) return
        val notification =
            NotificationCompat
                .Builder(context, PrayerNotificationChannels.SCHEDULE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(
                    context.getString(R.string.prayer_notification_title, context.getString(prayer.labelRes())),
                ).setContentText(context.getString(R.string.prayer_notification_body))
                .setContentIntent(openScheduleIntent(context))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun openScheduleIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_PRAYER_SCHEDULE, true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val EXTRA_PRAYER = "prayer"
        private const val EXTRA_TRACKS = "tracks"
        private const val NOTIFICATION_ID = 0x5A1B

        /** The alarm intent for [event]; a `null` event builds the bare intent used to cancel. */
        fun intent(
            context: Context,
            event: PrayerAlarmEvent?,
        ): Intent =
            Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(EXTRA_PRAYER, event?.prayer?.name)
                putExtra(EXTRA_TRACKS, event?.tracks?.map { it.name }?.toTypedArray())
            }
    }
}
