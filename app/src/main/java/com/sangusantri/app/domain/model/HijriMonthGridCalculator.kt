package com.sangusantri.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Builds [HijriCalendarMonth] — the Sunday-first six-week grid (CAL-FR-003). Hijri conversion
 * reuses the exact `HijrahDate`-based approach [ReminderScheduleCalculator]/[ReminderScheduleFormatter]
 * already use in production (CAL-FR-002); [PasaranCalculator] supplies the Pancawara name.
 */
object HijriMonthGridCalculator {
    fun build(
        yearMonth: YearMonth,
        today: LocalDate = LocalDate.now(),
    ): HijriCalendarMonth {
        val firstOfMonth = yearMonth.atDay(1)
        val sundayOffset = firstOfMonth.dayOfWeek.value % HijriCalendarMonth.WEEK_LENGTH
        val gridStart = firstOfMonth.minusDays(sundayOffset.toLong())
        val gridDates = (0 until HijriCalendarMonth.GRID_SIZE).map { gridStart.plusDays(it.toLong()) }

        val dotEvents = HijriAgendaCalculator.eventsIntersecting(gridDates).filterNot { it.isFlexibleWindow }

        val days =
            gridDates.map { date ->
                val hijrahDate = HijrahDate.from(date)
                HijriCalendarDay(
                    date = date,
                    hijriYear = hijrahDate.get(ChronoField.YEAR),
                    hijriMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR),
                    hijriDay = hijrahDate.get(ChronoField.DAY_OF_MONTH),
                    pasaran = PasaranCalculator.calculate(date),
                    isCurrentMonth = YearMonth.from(date) == yearMonth,
                    isToday = date == today,
                    isSunday = date.dayOfWeek == DayOfWeek.SUNDAY,
                    eventKinds = dotEvents.filter { it.intersects(date) }.map { it.kind }.toSet(),
                )
            }

        val currentMonthDays = days.filter { it.isCurrentMonth }
        return HijriCalendarMonth(
            yearMonth = yearMonth,
            days = days,
            hijriSpanStart = HijriYearMonth(currentMonthDays.first().hijriYear, currentMonthDays.first().hijriMonth),
            hijriSpanEnd = HijriYearMonth(currentMonthDays.last().hijriYear, currentMonthDays.last().hijriMonth),
        )
    }
}
