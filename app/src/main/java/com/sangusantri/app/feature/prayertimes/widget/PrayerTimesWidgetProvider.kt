package com.sangusantri.app.feature.prayertimes.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.getSystemService
import com.sangusantri.app.MainActivity
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.PrayerTime
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import com.sangusantri.app.feature.home.formatAsClock
import com.sangusantri.app.feature.home.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale
import javax.inject.Inject

/**
 * Home-screen widget for the next prayer time — the Beranda block, lifted onto the launcher.
 *
 * Reads the same [PrayerScheduleRepository] every other prayer surface reads, so it shows only what
 * Room already holds. No city chosen yet, or no schedule cached, means the empty state: this widget
 * never invents a time.
 *
 * **No countdown, by design.** A ticking widget has to be redrawn every minute for the rest of the
 * day, which is the one thing a home-screen widget must not do. Instead it shows values that stay
 * true until the next prayer, and re-renders once at that boundary via a single inexact,
 * doze-friendly alarm — five or six wake-ups a day rather than fourteen hundred. The platform's
 * 30-minute [R.xml.prayer_times_widget_info] update period is only a safety net behind that alarm
 * (it also picks up a city chosen while the widget is on screen).
 *
 * **Two shapes, one renderer.** [PrayerTimesWideWidgetProvider] is the horizontal band offered as a
 * second entry in the widget picker; it subclasses this one and adds nothing, because the two
 * differ only in the default size, resize envelope and preview their `appwidget-provider` XML
 * declares. Everything below already adapts to the reported size, so a user who resizes either one
 * gets the arrangement that fits rather than the one its picker entry started at.
 *
 * **Glass, not blur.** There is no backdrop-blur API for RemoteViews — they are inflated in the
 * launcher's process, which never hands a widget what is behind it. The panel is a translucent
 * *neutral* fill with a hairline edge instead: neutral so it sits against any wallpaper,
 * translucent so the wallpaper reads through, edged so it does not melt into it. Light/dark comes
 * from resource qualifiers (`values-night/widget_colors.xml`), i.e. the *system* setting rather
 * than the app's in-app sun/moon override, for the same reason.
 *
 * `TooManyFunctions` is suppressed because a provider has to cover four things at once: the
 * `AppWidgetProvider` lifecycle, building the panel, the boundary alarm, and the two
 * `PendingIntent`s. Splitting them across files would only move the same short functions somewhere
 * else — the equally cohesive `PrayerScheduleRepositoryImpl` carries the same suppression.
 */
@Suppress("TooManyFunctions")
@AndroidEntryPoint
open class PrayerTimesWidgetProvider : AppWidgetProvider() {
    @Inject
    lateinit var repository: PrayerScheduleRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        render(context, appWidgetManager, appWidgetIds)
    }

    /** Fires on resize and on rotation — the only signal that the 2x2/landscape choice may have
     * changed. */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        render(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onDisabled(context: Context) {
        context.getSystemService<AlarmManager>()?.cancel(flipPendingIntent(context))
    }

    private fun render(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Room + DataStore reads cannot happen on the broadcast thread, and the process may be
        // killed the moment onUpdate returns — goAsync is the standard way to hold it open.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data =
                    PanelData(
                        schedule = repository.observeToday().first(),
                        cityName = repository.observeSelectedCity().first()?.name,
                    )
                val now = LocalTime.now()
                appWidgetIds.forEach { id ->
                    appWidgetManager.updateAppWidget(id, buildViews(context, appWidgetManager, id, data, now))
                }
                scheduleNextFlip(context, data.schedule, now)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Every view whose visibility varies is set on **every** render, never left to the layout's own
     * `android:visibility`. A launcher reapplies a RemoteViews onto the views it is already showing
     * when the layout is unchanged, so the XML defaults are not restored between renders — anything
     * shown once and not explicitly hidden again stays on screen. The same reason both containers
     * are emptied even though only one is filled: `addView` appends to whatever is already there.
     */
    private fun buildViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        data: PanelData,
        now: LocalTime,
    ): RemoteViews {
        val schedule = data.schedule
        val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
        views.setOnClickPendingIntent(android.R.id.background, openScheduleIntent(context))

        val (widthDp, heightDp) = panelSizeDp(context, appWidgetManager.getAppWidgetOptions(appWidgetId))

        val prayers = schedule?.times.orEmpty()
        val wide = widthDp >= WIDE_MIN_WIDTH_DP
        // The date shares the header line with the city, so it is gated on width, not height: it
        // needs room beside the city name, and below that the city alone is the more useful half.
        // The header itself is what goes when the panel gets short — a one-row band on a landscape
        // home screen (~51dp) has room for the times and nothing else.
        val showDate = prayers.isNotEmpty() && widthDp >= DATE_MIN_WIDTH_DP
        val showHeader = heightDp >= HEADER_MIN_HEIGHT_DP

        views.applyArrangement(
            hasSchedule = prayers.isNotEmpty(),
            wide = wide,
            showDate = showDate,
            showHeader = showHeader,
        )

        // The chosen kabupaten/kota, not the app's name — and it comes from the selected city, not
        // from the schedule, because those two are known at different moments. Picking a city by
        // location detection selects it the instant detection resolves, while its month is still
        // being fetched; reading the name off the schedule alone showed "JADWAL SHOLAT" for that
        // whole window.
        views.setTextViewText(
            R.id.widget_location,
            (schedule?.location?.takeIf { it.isNotBlank() } ?: data.cityName)?.uppercase()
                ?: context.getString(R.string.widget_prayer_label).uppercase(),
        )
        if (prayers.isEmpty()) {
            views.setTextViewText(
                R.id.widget_empty,
                context.getString(
                    if (data.cityName == null) {
                        R.string.widget_prayer_empty_message
                    } else {
                        R.string.widget_prayer_schedule_pending
                    },
                ),
            )
            return views
        }

        if (showDate) {
            // The app's own offline hijri computation — never the prayer-times service's hijri
            // calendar (ADR 0018).
            val monthNames = context.resources.getStringArray(R.array.hijri_month_names).toList()
            views.setTextViewText(
                R.id.widget_header_date,
                LocalDate.now().formatCompactWithHijri(monthNames),
            )
        }

        // Imsak belongs to the schedule but is dropped from a cramped strip, exactly as Beranda's
        // block drops it — six columns across four cells leaves under 40dp each, and "MAGRIB" does
        // not fit in that at a legible size. The full list at 2x2 always has room for all six.
        val container = if (wide) R.id.widget_strip else R.id.widget_list
        val next = schedule?.nextAfter(now)?.name
        val shown =
            prayers.filter {
                !wide || widthDp >= SIX_COLUMN_MIN_WIDTH_DP || it.name != PrayerName.IMSAK
            }
        val sizes = textSizesSp(widthDp, columns = if (wide) shown.size else 1)
        shown.forEach { prayer ->
            views.addView(container, prayerItem(context, prayer, prayer.name == next, wide, sizes))
        }
        return views
    }

    /**
     * What one render needs from the repository.
     *
     * Two reads, not one, because the schedule and the chosen city become known at different
     * moments — see the header comment in [buildViews].
     */
    private data class PanelData(
        val schedule: PrayerSchedule?,
        val cityName: String?,
    )

    /**
     * The panel's width and height in dp for the orientation it is actually being shown in.
     *
     * `OPTION_APPWIDGET_MIN_*`/`MAX_*` are not "smallest and largest" — they are the two
     * orientations. MIN_WIDTH/MAX_HEIGHT is the widget in portrait, MAX_WIDTH/MIN_HEIGHT in
     * landscape. Reading MIN_HEIGHT unconditionally measures a 4x1 at its ~51dp landscape height
     * even on a portrait home screen, which hides the city line that fits comfortably in the ~102dp
     * it actually has.
     */
    private fun panelSizeDp(
        context: Context,
        options: Bundle,
    ): Pair<Int, Int> {
        val portrait =
            context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        val widthKey =
            if (portrait) {
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
            } else {
                AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
            }
        val heightKey =
            if (portrait) {
                AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
            } else {
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
            }
        return options.getInt(widthKey) to options.getInt(heightKey)
    }

    /** The visibility half of [buildViews], extracted only to keep that method readable. */
    private fun RemoteViews.applyArrangement(
        hasSchedule: Boolean,
        wide: Boolean,
        showDate: Boolean,
        showHeader: Boolean,
    ) {
        fun show(visible: Boolean) = if (visible) View.VISIBLE else View.GONE
        setViewVisibility(R.id.widget_header, show(showHeader))
        setViewVisibility(R.id.widget_divider_top, show(showHeader))
        setViewVisibility(R.id.widget_empty, show(!hasSchedule))
        setViewVisibility(R.id.widget_header_date, show(showDate))
        setViewVisibility(R.id.widget_list, show(hasSchedule && !wide))
        setViewVisibility(R.id.widget_strip, show(hasSchedule && wide))
        removeAllViews(R.id.widget_list)
        removeAllViews(R.id.widget_strip)
    }

    /**
     * One prayer, inflated rather than written out: a hand-built 2x2 list plus a wide strip would be
     * twenty-four TextViews in the panel layout, every one needing an id and hand-maintained.
     *
     * Four item layouts rather than two plus `setTextColor`, because a colour pushed from code is
     * baked into the RemoteViews at render time — it keeps the theme it was rendered under, and the
     * panel goes unreadable the moment the system flips light/dark before the next re-render. The
     * variant is chosen here; every colour in it is a resource the launcher resolves at inflation.
     *
     * The *next* prayer is the highlighted one — the entry being counted down to, never the one
     * that most recently passed. Jadwal Sholat and Beranda mark the same entry, so the three prayer
     * surfaces always agree on which row is lit.
     */
    private fun prayerItem(
        context: Context,
        prayer: PrayerTime,
        highlighted: Boolean,
        wide: Boolean,
        sizes: TextSizesSp,
    ): RemoteViews {
        val layout =
            when {
                wide && highlighted -> R.layout.widget_prayer_column_next
                wide -> R.layout.widget_prayer_column
                highlighted -> R.layout.widget_prayer_row_next
                else -> R.layout.widget_prayer_row
            }
        val item = RemoteViews(context.packageName, layout)
        item.setTextViewText(R.id.widget_row_name, context.getString(prayer.name.labelRes()))
        item.setTextViewText(R.id.widget_row_time, prayer.time.formatAsClock())
        item.setTextViewTextSize(R.id.widget_row_name, TypedValue.COMPLEX_UNIT_SP, sizes.name)
        item.setTextViewTextSize(R.id.widget_row_time, TypedValue.COMPLEX_UNIT_SP, sizes.time)
        return item
    }

    /**
     * Type that grows and shrinks with the space each entry actually gets, rather than one fixed
     * size that only suits one widget size on one device.
     *
     * Pushed in `COMPLEX_UNIT_SP`, so the user's own font-scale setting still applies on top —
     * shrinking here narrows the range, it never opts the widget out of accessibility sizing. The
     * floor is what that range is really about: it is set at the smallest size Android's own lint
     * accepts, so a cramped column truncates or the panel drops a column before the text becomes
     * unreadable.
     */
    private fun textSizesSp(
        widthDp: Int,
        columns: Int,
    ): TextSizesSp {
        val available = (widthDp - PANEL_PADDING_DP * 2).coerceAtLeast(1)
        // Shrinks only. Growing the type on a wide panel pushed the strip past the 102dp a 4x1
        // actually has and clipped the times in half — the band's height is the binding constraint,
        // and it does not grow with the width.
        val scale = (available.toFloat() / columns / REFERENCE_COLUMN_DP).coerceIn(0.85f, 1f)
        return TextSizesSp(
            name = (BASE_NAME_SP * scale).coerceAtLeast(MIN_NAME_SP),
            time = (BASE_TIME_SP * scale).coerceAtLeast(MIN_TIME_SP),
        )
    }

    private data class TextSizesSp(
        val name: Float,
        val time: Float,
    )

    /**
     * "Kam, 20 Agu · 7 Rabiul Awal" — the compact twin of `JadwalSholatScreen`'s `formatWithHijri`,
     * which prints the full "Kamis, 20 Agustus 2026 · 7 Rabiul Awal 1448".
     *
     * Same `HijrahDate` computation and the same month-name array; only the presentation is shorter,
     * because here the date shares one header line with the city name and the full form does not fit
     * beside it at four cells. The hijri year goes for the same reason — the panel is a glance
     * surface, and Jadwal Sholat carries the full line.
     */
    private fun LocalDate.formatCompactWithHijri(hijriMonthNames: List<String>): String {
        val hijri = HijrahDate.from(this)
        val monthName = hijriMonthNames.getOrNull(hijri.get(ChronoField.MONTH_OF_YEAR) - 1).orEmpty()
        val gregorian =
            format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("id-ID")))
        return "$gregorian · ${hijri.get(ChronoField.DAY_OF_MONTH)} $monthName"
    }

    /**
     * One alarm, at the earlier of the next prayer and the next midnight. Midnight matters because
     * `observeToday` is keyed on today's date and the "besok" tag stops being true there.
     *
     * Inexact and non-waking on purpose: nobody is looking at the home screen while the device
     * sleeps, so this costs nothing until the device is already awake. Same reasoning — and the
     * same avoidance of `SCHEDULE_EXACT_ALARM` — as `data/reminder/ReminderAlarmScheduler`.
     */
    private fun scheduleNextFlip(
        context: Context,
        schedule: PrayerSchedule?,
        now: LocalTime,
    ) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val today = LocalDate.now()
        val nextPrayerAt =
            schedule?.nextAfter(now)?.let { prayer ->
                LocalDateTime.of(if (schedule.nextIsTomorrow(now)) today.plusDays(1) else today, prayer.time)
            }
        val triggerAt = listOfNotNull(today.plusDays(1).atStartOfDay(), nextPrayerAt).min()
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + FLIP_MARGIN_MILLIS,
            flipPendingIntent(context),
        )
    }

    /** Keyed on the concrete provider, so the two picker entries keep separate alarms instead of
     * overwriting each other's through a shared request code. */
    private fun flipPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            javaClass.name.hashCode(),
            updateIntent(context, javaClass),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openScheduleIntent(context: Context): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_PRAYER_SCHEDULE, true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        return PendingIntent.getActivity(
            context,
            OPEN_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        /**
         * Re-renders every placed widget. Called when the app leaves the foreground, which is the
         * cheap way to pick up a city the user just chose or a month that just finished syncing —
         * the repository has no business knowing a widget exists.
         */
        fun refresh(context: Context) {
            PROVIDERS.forEach { provider ->
                if (widgetIds(context, provider).isNotEmpty()) {
                    context.sendBroadcast(updateIntent(context, provider))
                }
            }
        }

        /** The two entries the widget picker offers. Both render through this class. */
        private val PROVIDERS =
            listOf(
                PrayerTimesWidgetProvider::class.java,
                PrayerTimesWideWidgetProvider::class.java,
            )

        private fun widgetIds(
            context: Context,
            provider: Class<out PrayerTimesWidgetProvider>,
        ): IntArray =
            AppWidgetManager
                .getInstance(context)
                .getAppWidgetIds(ComponentName(context, provider))

        /**
         * `ACTION_APPWIDGET_UPDATE` rather than a private action: `AppWidgetProvider.onReceive`
         * already routes it to `onUpdate`, so no `onReceive` override is needed — and overriding it
         * under `@AndroidEntryPoint` is where injection ordering goes wrong. The id array is
         * required; the framework drops the broadcast without it.
         */
        private fun updateIntent(
            context: Context,
            provider: Class<out PrayerTimesWidgetProvider>,
        ): Intent =
            Intent(context, provider).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds(context, provider))
            }

        /** Roughly four home-screen cells — below it the panel shows the vertical list instead. */
        private const val WIDE_MIN_WIDTH_DP = 200

        /** Only a strip this wide can carry Imsak as a sixth column without crowding. */
        private const val SIX_COLUMN_MIN_WIDTH_DP = 300

        /** Below this the date has no room beside the city name and is dropped. */
        private const val DATE_MIN_WIDTH_DP = 250

        /** Matches `@dimen/widget_padding`; used only to estimate how much a column really gets. */
        private const val PANEL_PADDING_DP = 14

        /** The column width the base sizes were drawn for — a six-column strip at four cells. */
        private const val REFERENCE_COLUMN_DP = 37f
        private const val BASE_NAME_SP = 11f
        private const val BASE_TIME_SP = 13f

        /** Floors. The time keeps Android lint's 11sp minimum; the name is allowed one step under
         * it, because a legible 10sp "Magrib" beats an 11sp "Magr…" and the name is the shorter-
         * lived of the two. Below this the panel drops a column instead. */
        private const val MIN_NAME_SP = 10f
        private const val MIN_TIME_SP = 11f

        /** Below this even the city header goes, leaving the bare strip — the only thing that fits
         * a one-row band on a landscape home screen. */
        private const val HEADER_MIN_HEIGHT_DP = 70

        /** Fire just past the boundary, never a tick before it. */
        private const val FLIP_MARGIN_MILLIS = 2_000L
        private const val OPEN_REQUEST_CODE = 0xB2
    }
}
