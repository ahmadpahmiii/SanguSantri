package com.sangusantri.app.domain.model

import java.time.LocalDate

/**
 * One grid cell (CAL-FR-003). [eventKinds] excludes [HijriRecurringEventRule.isFlexibleWindow]
 * occurrences by construction (`HijriMonthGridCalculator`) — the six-days-of-Syawal guidance window
 * must never render a per-day dot (PRD §5.2). Selection state is intentionally not modelled here:
 * it is compared against [date] by the UI layer so the 42-cell list never needs rebuilding just
 * because the user tapped a different day.
 */
data class HijriCalendarDay(
    val date: LocalDate,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriDay: Int,
    val pasaran: Pasaran,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSunday: Boolean,
    val eventKinds: Set<HijriEventKind>,
) {
    val isOfficialHoliday: Boolean
        get() = HijriEventKind.NATIONAL_HOLIDAY in eventKinds || HijriEventKind.COLLECTIVE_LEAVE in eventKinds

    /** CAL-FR-007: only Sunday or a sourced official holiday emphasises the Gregorian numeral. */
    val isDateNumberEmphasized: Boolean get() = isSunday || isOfficialHoliday

    val hasFastingDot: Boolean get() = HijriEventKind.FASTING in eventKinds
    val hasObservanceDot: Boolean get() = eventKinds.any { it != HijriEventKind.FASTING }
}
