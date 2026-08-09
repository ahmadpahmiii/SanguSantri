package com.sangusantri.app.domain.model

import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Resolves [HijriCalendarBundle]'s rules into concrete [HijriCalendarEvent]s for whatever Hijri
 * months a caller cares about — pure, no Room/Context dependency, same "stateless calculator
 * object" shape as [ReminderScheduleCalculator]/[PasaranCalculator]. Rules are keyed by Hijri month
 * (CAL-FR-002/CAL-FR-006), so a rule is only ever evaluated for the specific Hijri (year, month)
 * pairs a Gregorian window actually touches — never the whole ten-year browse range at once.
 */
object HijriAgendaCalculator {
    /** Every event whose date range intersects any of [dates] — used to compute grid-cell dots,
     * including muted adjacent-month cells, which are real, navigable dates. */
    fun eventsIntersecting(dates: List<LocalDate>): List<HijriCalendarEvent> {
        if (dates.isEmpty()) return emptyList()
        val rangeStart = dates.min()
        val rangeEnd = dates.max()
        return eventsForHijriMonths(hijriYearMonthsOf(dates))
            .filter { it.intersects(rangeStart, rangeEnd) }
    }

    /** Every event intersecting the visible Gregorian [yearMonth] only — the agenda section's
     * "Semua" scope (§7.3), deliberately narrower than the grid's muted-cell window. */
    fun eventsForGregorianMonth(yearMonth: YearMonth): List<HijriCalendarEvent> {
        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        return eventsForHijriMonths(hijriYearMonthsOf(listOf(monthStart, monthEnd)))
            .filter { it.intersects(monthStart, monthEnd) }
    }

    private fun eventsForHijriMonths(hijriYearMonths: Set<Pair<Int, Int>>): List<HijriCalendarEvent> {
        val ruleOccurrences =
            hijriYearMonths.flatMap { (year, month) ->
                HijriCalendarBundle.rules
                    .filter { it.appliesToHijriMonth(month) }
                    .map { it.resolve(year, month) }
            }
        return (ruleOccurrences + HijriCalendarBundle.officialRecords)
            .distinctBy { it.id }
            .sortedBy { it.startDate }
    }

    /** Every distinct Hijri (year, month) any of [dates] falls in — the days between the earliest
     * and latest date are also sampled so a rule anchored just outside [dates] but overlapping it
     * (e.g. a Hijri month whose Gregorian span starts before the first sampled date) is not missed. */
    private fun hijriYearMonthsOf(dates: List<LocalDate>): Set<Pair<Int, Int>> {
        val start = dates.min()
        val end = dates.max()
        val months = mutableSetOf<Pair<Int, Int>>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            val hijrahDate = HijrahDate.from(cursor)
            months += hijrahDate.get(ChronoField.YEAR) to hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            cursor = cursor.plusDays(1)
        }
        return months
    }
}
