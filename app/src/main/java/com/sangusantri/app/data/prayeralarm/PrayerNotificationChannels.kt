package com.sangusantri.app.data.prayeralarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.sangusantri.app.R

/**
 * Two channels, because the two modes are two different things to the system.
 *
 * [SCHEDULE_CHANNEL_ID] is "notifikasi bawaan": no sound is set on it, so it takes the device's
 * default notification sound and vibration and stays under the reader's own notification profile.
 *
 * [ADZAN_CHANNEL_ID] is silent on purpose. Its notification is the foreground notification of
 * [AdzanPlaybackService], which is playing the recording itself on the alarm stream — a channel
 * sound here would ring underneath the adzan.
 */
object PrayerNotificationChannels {
    const val SCHEDULE_CHANNEL_ID = "jadwal_sholat"
    const val ADZAN_CHANNEL_ID = "adzan"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                SCHEDULE_CHANNEL_ID,
                context.getString(R.string.prayer_channel_schedule_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.prayer_channel_schedule_description)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ADZAN_CHANNEL_ID,
                context.getString(R.string.prayer_channel_adzan_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.prayer_channel_adzan_description)
                setSound(null, null)
                enableVibration(false)
            },
        )
    }
}
