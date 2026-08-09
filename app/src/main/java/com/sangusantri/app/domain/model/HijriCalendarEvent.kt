package com.sangusantri.app.domain.model

import java.time.LocalDate

/**
 * One resolved agenda occurrence — either a [HijriRecurringEventRule] applied to a concrete Hijri
 * year, or a future sourced official-date record. Deliberately carries only raw Hijri/Gregorian
 * fields, never a pre-formatted display string: month-name localisation belongs to the UI layer
 * (`feature/hijricalendar/HijriCalendarFormatter.kt`), matching how [domain.model.ReminderSchedule]
 * stays presentation-free and callers supply `R.array.hijri_month_names` themselves.
 *
 * [isFlexibleWindow] (six days of Syawal, PRD §5.2) means this occurrence must still render as one
 * agenda row but must never contribute a per-day dot — the window names a guidance range, not six
 * specific recommended dates.
 */
data class HijriCalendarEvent(
    val id: String,
    val kind: HijriEventKind,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriStartDay: Int,
    val hijriEndDay: Int,
    val isFlexibleWindow: Boolean,
    val calculationStatus: HijriCalculationStatus,
    val provenance: HijriEventProvenance,
) {
    val isMultiDay: Boolean get() = startDate != endDate

    fun intersects(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)

    fun intersects(
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
    ): Boolean = !startDate.isAfter(rangeEnd) && !endDate.isBefore(rangeStart)
}
