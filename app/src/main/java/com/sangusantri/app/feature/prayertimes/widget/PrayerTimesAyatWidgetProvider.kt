package com.sangusantri.app.feature.prayertimes.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sangusantri.app.MainActivity
import com.sangusantri.app.R

/**
 * The third picker entry: the prayer strip with the ayat of the day underneath.
 *
 * A subclass rather than a second renderer — the header, the divider, the strip, the empty state,
 * the size logic and the boundary alarm are all inherited unchanged. It overrides exactly three
 * things: the layout (which carries a second hairline and the quote line), the flag that makes the
 * base read the day's ayat, and where a tap goes.
 *
 * **4x2 by default, 4x4 at most, and never smaller than 4x2.** Its `appwidget-provider` XML pins
 * `minResizeWidth`/`minResizeHeight` to that floor, because the quote needs the full width to say
 * anything at all: at two cells it would be three words and an ellipsis. Dragging it taller is
 * meaningful rather than decorative — the ayat block takes the leftover height and the base raises
 * its line counts with it, so 4x4 shows the passage where 4x2 shows an opening line.
 *
 * **The quote stays quieter than the prayer times.** Arabic in `widget_strong`, translation in
 * `widget_dim`, neither in the accent — someone glancing at their home screen is looking for the
 * next prayer.
 */
class PrayerTimesAyatWidgetProvider : PrayerTimesWidgetProvider() {
    override val layoutRes: Int get() = R.layout.widget_prayer_times_ayat

    override val showsAyat: Boolean get() = true

    /**
     * Beranda, unlike the two schedule-only entries — the ayah header is the reason this panel
     * exists, and it lives there.
     *
     * No extra to carry: Beranda is the app's start destination, so a launch is a launch. The one
     * case this does not cover is the app already running on Aktivitas or Tasbih, where the tap
     * resumes that tab instead; routing across an already-built back stack would need a deep-link
     * extra threaded through the nav host, which is not worth it for that.
     */
    override fun openIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            OPEN_BERANDA_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        /** Distinct from the base's schedule request code, so the two PendingIntents do not
         * overwrite one another. */
        const val OPEN_BERANDA_REQUEST_CODE = 0xB3
    }
}
