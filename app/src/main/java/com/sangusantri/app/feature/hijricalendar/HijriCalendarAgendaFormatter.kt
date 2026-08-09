package com.sangusantri.app.feature.hijricalendar

import com.sangusantri.app.domain.model.HijriCalendarEvent
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Pure agenda-row display text — split out of [HijriCalendarFormatter] (which stays focused on
 * calendar/date text) to keep each object under detekt's function-count threshold. */
object HijriCalendarAgendaFormatter {
    private val INDONESIAN = Locale.forLanguageTag("in-ID")

    /** An agenda row's Hijri range text, e.g. "13–15 Rabiulawal 1448" or, for a flexible-window
     * event, "2 s.d. akhir Syawal 1448" — never a hard end day for a window that has none. */
    fun formatEventHijriRange(
        event: HijriCalendarEvent,
        hijriMonthNames: List<String>,
    ): String {
        val monthName = hijriMonthNames.getOrElse(event.hijriMonth - 1) { "" }
        return when {
            event.isFlexibleWindow -> "${event.hijriStartDay} s.d. akhir $monthName ${event.hijriYear}"
            event.hijriStartDay == event.hijriEndDay -> "${event.hijriStartDay} $monthName ${event.hijriYear}"
            else -> "${event.hijriStartDay}–${event.hijriEndDay} $monthName ${event.hijriYear}"
        }
    }

    fun formatAgendaBadgeMonth(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.SHORT, INDONESIAN).replaceFirstChar { it.uppercase(INDONESIAN) }

    fun formatAgendaBadgeDay(event: HijriCalendarEvent): String =
        if (event.isMultiDay) {
            "${event.startDate.dayOfMonth}–${event.endDate.dayOfMonth}"
        } else {
            "${event.startDate.dayOfMonth}"
        }
}
