package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** CAL-FR-004: anchor, negative offsets, leap years, and representative historical/future dates. */
class PasaranCalculatorTest {
    @Test
    fun anchorDateIsLegi() {
        assertEquals(Pasaran.LEGI, PasaranCalculator.calculate(LocalDate.of(1633, 7, 8)))
    }

    @Test
    fun oneDayBeforeAnchorWrapsBackwardToKliwon() {
        assertEquals(Pasaran.KLIWON, PasaranCalculator.calculate(LocalDate.of(1633, 7, 7)))
    }

    @Test
    fun sixDaysAfterAnchorCompletesOneCyclePlusOne() {
        assertEquals(Pasaran.PAHING, PasaranCalculator.calculate(LocalDate.of(1633, 7, 14)))
    }

    @Test
    fun leapDayIsCalculatedCorrectly() {
        assertEquals(Pasaran.LEGI, PasaranCalculator.calculate(LocalDate.of(2024, 2, 29)))
    }

    @Test
    fun tenYearsBeforeCurrentEraDate() {
        assertEquals(Pasaran.KLIWON, PasaranCalculator.calculate(LocalDate.of(2016, 8, 8)))
    }

    @Test
    fun tenYearsAfterCurrentEraDate() {
        assertEquals(Pasaran.KLIWON, PasaranCalculator.calculate(LocalDate.of(2036, 8, 8)))
    }

    @Test
    fun matchesApprovedDesignFixture8August2026() {
        assertEquals(Pasaran.PAHING, PasaranCalculator.calculate(LocalDate.of(2026, 8, 8)))
    }

    @Test
    fun matchesApprovedDesignFixtureSequenceAcrossAWeek() {
        val expected =
            listOf(
                LocalDate.of(2026, 7, 26) to Pasaran.WAGE,
                LocalDate.of(2026, 7, 27) to Pasaran.KLIWON,
                LocalDate.of(2026, 7, 28) to Pasaran.LEGI,
                LocalDate.of(2026, 7, 29) to Pasaran.PAHING,
                LocalDate.of(2026, 7, 30) to Pasaran.PON,
                LocalDate.of(2026, 7, 31) to Pasaran.WAGE,
                LocalDate.of(2026, 8, 1) to Pasaran.KLIWON,
            )
        expected.forEach { (date, pasaran) -> assertEquals(pasaran, PasaranCalculator.calculate(date)) }
    }
}
