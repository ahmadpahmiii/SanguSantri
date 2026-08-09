package com.sangusantri.app.domain.model

import java.time.YearMonth

/** A stable 42-cell (six-week) Sunday-first grid for one Gregorian [yearMonth] (CAL-FR-003).
 * [hijriSpanStart]/[hijriSpanEnd] are the grid's own currently-visible-month days only (not the
 * muted adjacent-month cells) — the month heading's "Safar – Rabiulawal 1448" span. */
data class HijriCalendarMonth(
    val yearMonth: YearMonth,
    val days: List<HijriCalendarDay>,
    val hijriSpanStart: HijriYearMonth,
    val hijriSpanEnd: HijriYearMonth,
) {
    init {
        require(
            days.size == GRID_SIZE,
        ) { "Kalender Hijriah grid must have exactly $GRID_SIZE cells, got ${days.size}." }
    }

    companion object {
        const val GRID_SIZE = 42
        const val WEEK_LENGTH = 7
    }
}
