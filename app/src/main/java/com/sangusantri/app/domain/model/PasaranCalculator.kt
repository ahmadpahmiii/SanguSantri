package com.sangusantri.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure Pancawara (Pasaran) math — no Room/Context dependency, same "pure calculator object" shape
 * as [ReminderScheduleCalculator]. The cycle repeats every 5 days; [ANCHOR_DATE] is the documented
 * Friday Legi, 8 July 1633 reference anchor (`docs/product/HIJRI_CALENDAR_PRD.md` §3.3, CAL-FR-004),
 * expressed as a proleptic-Gregorian [LocalDate] so the same modular arithmetic that
 * `kalenderjawa.github.io` and other Pancawara references use also holds here.
 */
object PasaranCalculator {
    private val ANCHOR_DATE: LocalDate = LocalDate.of(1633, 7, 8)
    private val CYCLE = Pasaran.entries
    private const val CYCLE_LENGTH = 5

    fun calculate(date: LocalDate): Pasaran {
        val daysSinceAnchor = ChronoUnit.DAYS.between(ANCHOR_DATE, date)
        val index = Math.floorMod(daysSinceAnchor, CYCLE_LENGTH.toLong()).toInt()
        return CYCLE[index]
    }
}
