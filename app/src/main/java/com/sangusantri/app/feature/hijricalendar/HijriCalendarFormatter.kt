package com.sangusantri.app.feature.hijricalendar

import com.sangusantri.app.domain.model.HijriCalendarDay
import com.sangusantri.app.domain.model.HijriYearMonth
import com.sangusantri.app.domain.model.Pasaran
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure Indonesian display text for Kalender Hijriah — no Context/Composable dependency, same
 * "caller supplies the localised month-name array" shape as
 * [com.sangusantri.app.feature.reminder.ReminderScheduleFormatter]. Arabic-Indic digits
 * ([toArabicIndicDigits]) are visual-only (CAL-FR-005): every other value here stays Latin/plain
 * Indonesian text so TalkBack always announces an ordinary spoken date.
 */
object HijriCalendarFormatter {
    private val INDONESIAN = Locale.forLanguageTag("in-ID")
    private val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    private val GREGORIAN_FULL_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", INDONESIAN)
    private val GREGORIAN_MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIAN)
    private const val DAYS_PER_WEEK = 7

    fun toArabicIndicDigits(value: Int): String =
        value
            .toString()
            .map { char -> if (char.isDigit()) ARABIC_INDIC_DIGITS[char - '0'] else char }
            .joinToString(separator = "")

    /** [weekdayNames] must be Sunday-first ("Ahad", CAL-FR §7.1) — never `date.dayOfWeek`'s own
     * `getDisplayName`, which resolves Indonesian Sunday as "Minggu", not this app's "Ahad". */
    fun formatWeekdayAndPasaran(
        date: LocalDate,
        pasaran: Pasaran,
        weekdayNames: List<String>,
        pasaranNames: List<String>,
    ): String {
        val weekday = weekdayNames.getOrElse(sundayFirstIndex(date)) { "" }
        val pasaranName = pasaranNames.getOrElse(pasaran.ordinal) { "" }
        return "$weekday $pasaranName"
    }

    /** [weekdayNames] must be Sunday-first — see [formatWeekdayAndPasaran]. */
    fun formatWeekdayFull(
        date: LocalDate,
        weekdayNames: List<String>,
    ): String = weekdayNames.getOrElse(sundayFirstIndex(date)) { "" }

    private fun sundayFirstIndex(date: LocalDate): Int = date.dayOfWeek.value % DAYS_PER_WEEK

    fun formatGregorianFull(date: LocalDate): String = date.format(GREGORIAN_FULL_DATE)

    fun formatGregorianMonthYear(yearMonth: YearMonth): String = yearMonth.atDay(1).format(GREGORIAN_MONTH_YEAR)

    fun formatHijriFull(
        year: Int,
        month: Int,
        day: Int,
        hijriMonthNames: List<String>,
    ): String {
        val monthName = hijriMonthNames.getOrElse(month - 1) { "" }
        return "$day $monthName $year"
    }

    /** The month header subtitle, e.g. "Safar – Rabiulawal 1448" or "Ramadan 1447" when the whole
     * visible month sits inside a single Hijri month. */
    fun formatHijriMonthSpan(
        start: HijriYearMonth,
        end: HijriYearMonth,
        hijriMonthNames: List<String>,
    ): String {
        val startName = hijriMonthNames.getOrElse(start.month - 1) { "" }
        if (start == end) return "$startName ${start.year}"
        val endName = hijriMonthNames.getOrElse(end.month - 1) { "" }
        return if (start.year == end.year) {
            "$startName – $endName ${end.year}"
        } else {
            "$startName ${start.year} – $endName ${end.year}"
        }
    }

    /** Combined Gregorian + Hijri text for the top bar and selected-date summary, e.g.
     * "8 Agustus 2026 · 25 Safar 1448". */
    fun formatSelectedDateSubtitle(
        day: HijriCalendarDay,
        hijriMonthNames: List<String>,
    ): String =
        "${formatGregorianFull(
            day.date,
        )} · ${formatHijriFull(day.hijriYear, day.hijriMonth, day.hijriDay, hijriMonthNames)}"
}
