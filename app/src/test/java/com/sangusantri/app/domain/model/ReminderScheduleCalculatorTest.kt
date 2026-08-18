package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * The reminder form's preview text, `ScheduleReminderUseCase`, and the boot-reschedule use case all
 * read their trigger time from here, so a disagreement anywhere in this math shows up as a reminder
 * that fires at the wrong time or never re-arms.
 *
 * Every case pins an explicit `now` — a calculator that only behaves correctly relative to the
 * wall clock is not testable and not trustworthy.
 */
class ReminderScheduleCalculatorTest {
    private val jakarta = ZoneId.of("Asia/Jakarta")

    // Thursday 2026-08-13, 09:00 local.
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 8, 13, 9, 0, 0, 0, jakarta)

    @Test
    fun weeklyLaterTodayTriggersToday() {
        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.THURSDAY, hour = 18), now)

        assertEquals(ZonedDateTime.of(2026, 8, 13, 18, 0, 0, 0, jakarta), next)
    }

    @Test
    fun weeklyEarlierTodayRollsToNextWeek() {
        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.THURSDAY, hour = 6), now)

        assertEquals(ZonedDateTime.of(2026, 8, 20, 6, 0, 0, 0, jakarta), next)
    }

    @Test
    fun weeklyExactlyNowRollsToNextWeekRatherThanFiringImmediately() {
        // candidate.isAfter(now) is strict, and it has to be: an alarm scheduled for the instant it
        // is computed would fire before the user finished creating it.
        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.THURSDAY, hour = 9, minute = 0), now)

        assertEquals(ZonedDateTime.of(2026, 8, 20, 9, 0, 0, 0, jakarta), next)
    }

    @Test
    fun weeklyLaterInTheWeekTriggersThisWeek() {
        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.SUNDAY, hour = 5), now)

        assertEquals(ZonedDateTime.of(2026, 8, 16, 5, 0, 0, 0, jakarta), next)
    }

    @Test
    fun weeklyEarlierInTheWeekWrapsForward() {
        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.MONDAY, hour = 5), now)

        assertEquals(ZonedDateTime.of(2026, 8, 17, 5, 0, 0, 0, jakarta), next)
    }

    @Test
    fun weeklyTriggerZeroesSecondsAndNanos() {
        val messyNow = now.withSecond(37).withNano(123_456_789)

        val next = ReminderScheduleCalculator.nextTrigger(weekly(DayOfWeek.FRIDAY, hour = 18), messyNow)

        assertEquals(0, next.second)
        assertEquals(0, next.nano)
    }

    @Test
    fun hijriDateAlreadyPassedThisYearRollsToNextHijriYear() {
        val schedule = hijri(month = 1, day = 10, hour = 5)

        val next = ReminderScheduleCalculator.nextTrigger(schedule, now)

        assertTrue("expected a future trigger, got $next", next.isAfter(now))
    }

    @Test
    fun hijriDateTriggerIsAlwaysInTheFuture() {
        // Whatever the Hijri/Gregorian offset happens to be on the fixed `now` above, the contract
        // that matters is the same for every month: never schedule an alarm in the past.
        (1..12).forEach { month ->
            val next = ReminderScheduleCalculator.nextTrigger(hijri(month = month, day = 1, hour = 5), now)

            assertTrue("month $month produced a past trigger: $next", next.isAfter(now))
        }
    }

    @Test
    fun hijriTriggerKeepsTheRequestedWallClockTimeAndZone() {
        val next = ReminderScheduleCalculator.nextTrigger(hijri(month = 9, day = 1, hour = 4, minute = 30), now)

        assertEquals(4, next.hour)
        assertEquals(30, next.minute)
        assertEquals(jakarta, next.zone)
    }

    @Test
    fun hijriMonthLengthIsTwentyNineOrThirtyDays() {
        (1..12).forEach { month ->
            val length = ReminderScheduleCalculator.hijriMonthLength(hijriYear = 1448, hijriMonth = month)

            assertTrue("month $month reported $length days", length == 29 || length == 30)
        }
    }

    private fun weekly(
        dayOfWeek: DayOfWeek,
        hour: Int,
        minute: Int = 0,
    ) = ReminderSchedule.Weekly(dayOfWeek = dayOfWeek, hour = hour, minute = minute)

    private fun hijri(
        month: Int,
        day: Int,
        hour: Int,
        minute: Int = 0,
    ) = ReminderSchedule.HijriDate(
        hijriMonth = month,
        hijriDay = day,
        hour = hour,
        minute = minute,
        repeatsYearly = true,
    )
}
