package com.sangusantri.app.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.sangusantri.app.R

/**
 * One notification channel for every reminder (`0.0.4`) — created once at app startup
 * (`SanguSantriApplication.onCreate`). `minSdk` is 26 (`Build.VERSION_CODES.O`), the same version
 * notification channels became mandatory, so there is no pre-O branch to skip.
 */
object ReminderNotificationChannel {
    const val CHANNEL_ID = "pengingat_amaliyah"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.reminder_notification_channel_description)
            }
        manager.createNotificationChannel(channel)
    }
}
