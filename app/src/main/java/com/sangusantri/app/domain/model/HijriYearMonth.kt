package com.sangusantri.app.domain.model

/** A raw Hijri (year, month) pair — no display string, so [HijriMonthGridCalculator]'s output stays
 * presentation-free (see [HijriCalendarEvent]'s file comment for why). */
data class HijriYearMonth(
    val year: Int,
    val month: Int,
)
