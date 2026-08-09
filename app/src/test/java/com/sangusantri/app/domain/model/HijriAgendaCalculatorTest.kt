package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/**
 * CAL-FR-006 (merge/group agenda rules) and acceptance criteria 7–9 (no Puasa Senin–Kamis, Ayyamul
 * Bidh excludes 13 Zulhijah, Idul Fitri/Idul Adha/Tasyrik never recommended as fasts). Assertions
 * stay in terms of raw Hijri fields ([HijriRecurringEventRule.resolve]'s `hijriStartDay`/
 * `hijriEndDay`) rather than converted Gregorian dates, since the JVM running these unit tests may
 * not share Android's exact `HijrahDate` (Umm al-Qura) table — only [HijriMonthGridCalculatorTest]
 * needs that conversion and is scoped to structural invariants for the same reason.
 */
class HijriAgendaCalculatorTest {
    private fun rule(id: String) = HijriCalendarBundle.rules.first { it.id == id }

    @Test
    fun bundleNeverContainsPuasaSeninKamis() {
        val hasWeeklyFast =
            HijriCalendarBundle.rules.any { rule ->
                val text = "${rule.id} ${rule.title}".lowercase()
                "senin" in text || "kamis" in text
            }
        assertFalse(hasWeeklyFast)
    }

    @Test
    fun ayyamulBidhGeneralRuleExcludesZulhijjah() {
        val general = rule("ayyamul-bidh")
        assertFalse(general.appliesToHijriMonth(HIJRI_MONTH_ZULHIJJAH))
        assertTrue(general.appliesToHijriMonth(HIJRI_MONTH_RABIULAWAL))
    }

    @Test
    fun ayyamulBidhZulhijjahRuleStartsAfterTasyrikBoundary() {
        val zulhijjahRule = rule("ayyamul-bidh-zulhijjah")
        val event = zulhijjahRule.resolve(1448, HIJRI_MONTH_ZULHIJJAH)
        assertEquals(14, event.hijriStartDay)
        assertEquals(15, event.hijriEndDay)
    }

    @Test
    fun ayyamulBidhIsOneRangeEventNotThreeDailyRows() {
        val event = rule("ayyamul-bidh").resolve(1448, HIJRI_MONTH_RABIULAWAL)
        assertEquals(13, event.hijriStartDay)
        assertEquals(15, event.hijriEndDay)
        assertTrue(event.isMultiDay)
    }

    @Test
    fun idulFitriIdulAdhaAndTasyrikAreNeverFastingRecommendations() {
        listOf("idul-fitri", "idul-adha", "tasyrik").forEach { id ->
            assertEquals(HijriEventKind.FASTING_PROHIBITED, rule(id).kind)
        }
    }

    @Test
    fun ramadanEndDayResolvesToActualHijriMonthLength() {
        val hijriYear = 1447
        val expectedLength = ReminderScheduleCalculator.hijriMonthLength(hijriYear, HIJRI_MONTH_RAMADAN)
        val event = rule("ramadan").resolve(hijriYear, HIJRI_MONTH_RAMADAN)
        assertEquals(1, event.hijriStartDay)
        assertEquals(expectedLength, event.hijriEndDay)
    }

    @Test
    fun sixDaysOfSyawalIsFlexibleAndNeverHardensIntoSixDots() {
        val event = rule("syawal-enam-hari").resolve(1448, HIJRI_MONTH_SYAWAL)
        assertTrue(event.isFlexibleWindow)
    }

    @Test
    fun eventsForGregorianMonthNeverDuplicatesAnIdAndStaysSorted() {
        val events = HijriAgendaCalculator.eventsForGregorianMonth(YearMonth.of(2026, 8))
        assertEquals(events.map { it.id }.distinct().size, events.size)
        assertEquals(events.sortedBy { it.startDate }, events)
    }

    @Test
    fun everyRuleCarriesProvenance() {
        HijriCalendarBundle.rules.forEach { rule ->
            assertTrue(rule.provenance.sourceUrl.startsWith("https://"))
            assertTrue(rule.provenance.sourcePublisher.isNotBlank())
        }
    }

    private companion object {
        const val HIJRI_MONTH_RABIULAWAL = 3
        const val HIJRI_MONTH_RAMADAN = 9
        const val HIJRI_MONTH_SYAWAL = 10
        const val HIJRI_MONTH_ZULHIJJAH = 12
    }
}
