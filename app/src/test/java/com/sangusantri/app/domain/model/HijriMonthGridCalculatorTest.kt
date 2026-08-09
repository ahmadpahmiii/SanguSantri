package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * CAL-FR-003 structural invariants — Sunday-first, always 42 cells, correct current-month count and
 * today marker. Deliberately does not assert specific converted Hijri year/month/day values: those
 * come from `java.time.chrono.HijrahDate`, which a JVM unit test may resolve against different Umm
 * al-Qura table data than the Android runtime the feature actually ships on.
 */
class HijriMonthGridCalculatorTest {
    @Test
    fun gridAlwaysHasFortyTwoCells() {
        val month = HijriMonthGridCalculator.build(YearMonth.of(2026, 8))
        assertEquals(42, month.days.size)
    }

    @Test
    fun gridIsSundayFirst() {
        val month = HijriMonthGridCalculator.build(YearMonth.of(2026, 8))
        assertEquals(
            DayOfWeek.SUNDAY,
            month.days
                .first()
                .date.dayOfWeek,
        )
        assertEquals(
            DayOfWeek.SATURDAY,
            month.days
                .last()
                .date.dayOfWeek,
        )
    }

    @Test
    fun currentMonthCellCountMatchesMonthLength() {
        val yearMonth = YearMonth.of(2026, 8)
        val month = HijriMonthGridCalculator.build(yearMonth)
        val currentMonthDays = month.days.filter { it.isCurrentMonth }
        assertEquals(yearMonth.lengthOfMonth(), currentMonthDays.size)
        assertTrue(currentMonthDays.all { it.date.month == yearMonth.month })
    }

    @Test
    fun todayMarkerMatchesSuppliedToday() {
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 8)
        val month = HijriMonthGridCalculator.build(yearMonth, today)
        val todayCells = month.days.filter { it.isToday }
        assertEquals(1, todayCells.size)
        assertEquals(today, todayCells.single().date)
    }

    @Test
    fun outsideCurrentYearMonthDoesNotMarkAnyDayAsToday() {
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2020, 1, 1)
        val month = HijriMonthGridCalculator.build(yearMonth, today)
        assertTrue(month.days.none { it.isToday })
    }

    @Test
    fun sundaysAreMarkedRegardlessOfCurrentMonth() {
        val month = HijriMonthGridCalculator.build(YearMonth.of(2026, 8))
        assertTrue(month.days.filter { it.isSunday }.all { it.date.dayOfWeek == DayOfWeek.SUNDAY })
        assertEquals(6, month.days.count { it.isSunday })
    }
}
