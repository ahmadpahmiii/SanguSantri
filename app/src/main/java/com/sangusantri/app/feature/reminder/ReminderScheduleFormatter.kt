package com.sangusantri.app.feature.reminder

import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.ReminderSchedule
import com.sangusantri.app.feature.reminder.ReminderScheduleFormatter.formatScheduleSummary
import java.time.Instant
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * Pure Indonesian schedule text (ROADMAP.md: "Gregorian and Hijri date") — no Context dependency,
 * reused by the reminder list, the create/edit form's live preview, and both entry-point sections.
 * Gregorian day-of-week names come from `java.time`'s own Indonesian locale data (well-supported
 * CLDR data); Hijri month names are not reliably localised across Android OEMs, so
 * [hijriMonthNames] must be supplied by the caller from `R.array.hijri_month_names` — the one
 * canonical table, shared with Kalender Hijriah (`0.0.7`, CAL-FR-002).
 */
object ReminderScheduleFormatter {
    private val INDONESIAN = Locale.forLanguageTag("in-ID")

    /** e.g. "Setiap Kamis, 19:00" or "15 Ramadan, 05:00 (setiap tahun)". */
    fun formatScheduleSummary(
        schedule: ReminderSchedule,
        hijriMonthNames: List<String>,
    ): String {
        val time = "%02d:%02d".format(schedule.hour, schedule.minute)
        return when (schedule) {
            is ReminderSchedule.Weekly -> {
                val dayName = schedule.dayOfWeek.getDisplayName(TextStyle.FULL, INDONESIAN)
                "Setiap $dayName, $time"
            }

            is ReminderSchedule.HijriDate -> {
                val monthName = hijriMonthNames.getOrElse(schedule.hijriMonth - 1) { "" }
                val recurrence = if (schedule.repeatsYearly) " (setiap tahun)" else ""
                "${schedule.hijriDay} $monthName, $time$recurrence"
            }
        }
    }

    /** The Hijri-calendar equivalent of [Reminder.nextTriggerAtEpochMillis] — shown as secondary,
     * contextual text alongside the Gregorian-anchored [formatScheduleSummary]. */
    fun formatNextTriggerHijri(
        reminder: Reminder,
        hijriMonthNames: List<String>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String {
        val hijrahDate = HijrahDate.from(Instant.ofEpochMilli(reminder.nextTriggerAtEpochMillis).atZone(zoneId))
        val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
        val month = hijriMonthNames.getOrElse(hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1) { "" }
        val year = hijrahDate.get(ChronoField.YEAR)
        return "$day $month $year H"
    }
}
