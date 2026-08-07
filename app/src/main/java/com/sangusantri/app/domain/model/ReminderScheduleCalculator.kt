package com.sangusantri.app.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Pure next-occurrence math for [ReminderSchedule] — no Room/Context/AlarmManager dependency, so
 * the create/edit form's live preview text, [com.sangusantri.app.domain.usecase.ScheduleReminderUseCase],
 * and the boot-reschedule use case all compute the identical trigger time. Hijri conversion uses
 * `java.time.chrono.HijrahDate`, built into Android API 26+ (this project's `minSdk`) — no new
 * dependency.
 */
object ReminderScheduleCalculator {
    private const val DAYS_PER_WEEK = 7L

    fun nextTrigger(
        schedule: ReminderSchedule,
        now: ZonedDateTime = ZonedDateTime.now(),
    ): ZonedDateTime =
        when (schedule) {
            is ReminderSchedule.Weekly -> nextWeeklyTrigger(schedule, now)
            is ReminderSchedule.HijriDate -> nextHijriTrigger(schedule, now)
        }

    /** The Hijri month length for [hijriYear]/[hijriMonth] — the creation form uses this to only
     * ever offer a valid day-of-month (Hijri months vary 29–30 days). */
    fun hijriMonthLength(
        hijriYear: Int,
        hijriMonth: Int,
    ): Int = HijrahDate.of(hijriYear, hijriMonth, 1).lengthOfMonth()

    /** The current Hijri (year, month, day) — the creation form's default when adding a Hijri-date reminder. */
    fun currentHijriDate(now: ZonedDateTime = ZonedDateTime.now()): HijrahDate = HijrahDate.from(now.toLocalDate())

    private fun nextWeeklyTrigger(
        schedule: ReminderSchedule.Weekly,
        now: ZonedDateTime,
    ): ZonedDateTime {
        val atTime =
            now
                .withHour(schedule.hour)
                .withMinute(schedule.minute)
                .withSecond(0)
                .withNano(0)
        val daysUntil = (schedule.dayOfWeek.value - atTime.dayOfWeek.value + DAYS_PER_WEEK) % DAYS_PER_WEEK
        val candidate = atTime.plusDays(daysUntil)
        return if (candidate.isAfter(now)) candidate else candidate.plusWeeks(1)
    }

    private fun nextHijriTrigger(
        schedule: ReminderSchedule.HijriDate,
        now: ZonedDateTime,
    ): ZonedDateTime {
        val zone = now.zone
        val currentHijriYear = currentHijriDate(now).get(ChronoField.YEAR)
        val thisYear = toZonedDateTime(currentHijriYear, schedule, zone)
        if (thisYear.isAfter(now)) return thisYear
        return toZonedDateTime(currentHijriYear + 1, schedule, zone)
    }

    private fun toZonedDateTime(
        hijriYear: Int,
        schedule: ReminderSchedule.HijriDate,
        zone: java.time.ZoneId,
    ): ZonedDateTime {
        val hijrahDate = HijrahDate.of(hijriYear, schedule.hijriMonth, schedule.hijriDay)
        return LocalDate.from(hijrahDate).atTime(schedule.hour, schedule.minute).atZone(zone)
    }
}
