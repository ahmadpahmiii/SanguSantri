package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The selector's contract is what makes Beranda and the home-screen widget agree without sharing
 * state: same date, same ayat, in any process, forever.
 */
class AyatHariIniSelectorTest {
    @Test
    fun sameDateAlwaysGivesTheSameAyat() {
        val date = LocalDate.of(2026, 8, 22)
        assertEquals(ayatOfDay(date), ayatOfDay(date))
    }

    @Test
    fun consecutiveDaysGiveDifferentAyat() {
        val date = LocalDate.of(2026, 8, 22)
        assertNotEquals(ayatOfDay(date), ayatOfDay(date.plusDays(1)))
    }

    @Test
    fun everyOrdinalStaysInsideTheMushaf() {
        val start = LocalDate.of(2026, 1, 1)
        (0 until 400).forEach { offset ->
            val ordinal = ayatOfDay(start.plusDays(offset.toLong()))
            assertTrue("ordinal $ordinal out of range", ordinal in 0 until QURAN_AYAT_COUNT)
        }
    }

    @Test
    fun aYearOfDatesVisitsManyDifferentAyat() {
        val start = LocalDate.of(2026, 1, 1)
        val distinct = (0 until 365).map { ayatOfDay(start.plusDays(it.toLong())) }.distinct()
        assertTrue("only ${distinct.size} distinct ayat in a year", distinct.size > 350)
    }
}
