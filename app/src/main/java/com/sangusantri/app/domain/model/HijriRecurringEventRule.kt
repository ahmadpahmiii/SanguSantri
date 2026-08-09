package com.sangusantri.app.domain.model

import java.time.LocalDate
import java.time.chrono.HijrahDate

/**
 * One audited allowlist rule (`docs/product/HIJRI_CALENDAR_PRD.md` §5.2) — a Hijri-recurring day or
 * day range, resolved fresh for every Hijri year it is asked about rather than a hardcoded per-year
 * Gregorian date. [hijriMonth] `null` means "every Hijri month" (Ayyamul Bidh's general case);
 * [excludedHijriMonths] lets that general rule exclude Zulhijah, which has its own narrower rule
 * (13 Zulhijah is Tasyrik, not Ayyamul Bidh). [endDay] `null` means "the last day of the Hijri
 * month", resolved dynamically via [ReminderScheduleCalculator.hijriMonthLength] so Ramadan's 29-vs-
 * 30-day length never needs a second policy.
 */
data class HijriRecurringEventRule(
    val id: String,
    val kind: HijriEventKind,
    val title: String,
    val description: String,
    val hijriMonth: Int?,
    val startDay: Int,
    val endDay: Int?,
    val provenance: HijriEventProvenance,
    val excludedHijriMonths: Set<Int> = emptySet(),
    val isFlexibleWindow: Boolean = false,
    val calculationStatus: HijriCalculationStatus = HijriCalculationStatus.UMM_AL_QURA_CALCULATION,
) {
    fun appliesToHijriMonth(month: Int): Boolean =
        (hijriMonth == null || hijriMonth == month) && month !in excludedHijriMonths

    /** Resolves this rule for one concrete Hijri [year]/[month] into a dated [HijriCalendarEvent]. */
    fun resolve(
        year: Int,
        month: Int,
    ): HijriCalendarEvent {
        val resolvedEndDay = endDay ?: ReminderScheduleCalculator.hijriMonthLength(year, month)
        val startDate = LocalDate.from(HijrahDate.of(year, month, startDay))
        val endDate = LocalDate.from(HijrahDate.of(year, month, resolvedEndDay))
        return HijriCalendarEvent(
            id = "$id-$year",
            kind = kind,
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            hijriYear = year,
            hijriMonth = month,
            hijriStartDay = startDay,
            hijriEndDay = resolvedEndDay,
            isFlexibleWindow = isFlexibleWindow,
            calculationStatus = calculationStatus,
            provenance = provenance,
        )
    }
}
