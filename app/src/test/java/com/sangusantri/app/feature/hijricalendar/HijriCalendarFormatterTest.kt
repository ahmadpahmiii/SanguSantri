package com.sangusantri.app.feature.hijricalendar

import com.sangusantri.app.domain.model.HijriCalculationStatus
import com.sangusantri.app.domain.model.HijriCalendarEvent
import com.sangusantri.app.domain.model.HijriEventKind
import com.sangusantri.app.domain.model.HijriEventProvenance
import com.sangusantri.app.domain.model.HijriYearMonth
import com.sangusantri.app.domain.model.Pasaran
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

private val HIJRI_MONTH_NAMES =
    listOf(
        "Muharram",
        "Safar",
        "Rabiul Awal",
        "Rabiul Akhir",
        "Jumadil Awal",
        "Jumadil Akhir",
        "Rajab",
        "Syaban",
        "Ramadan",
        "Syawal",
        "Dzulkaidah",
        "Dzulhijjah",
    )

private val WEEKDAY_NAMES = listOf("Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

class HijriCalendarFormatterTest {
    @Test
    fun toArabicIndicDigitsConvertsEachDigit() {
        assertEquals("٠", HijriCalendarFormatter.toArabicIndicDigits(0))
        assertEquals("٢٥", HijriCalendarFormatter.toArabicIndicDigits(25))
        assertEquals("١٤٤٨", HijriCalendarFormatter.toArabicIndicDigits(1448))
    }

    @Test
    fun formatWeekdayFullUsesAhadForSundayNeverMinggu() {
        // 9 August 2026 is a Sunday — java.time's own Indonesian `getDisplayName` would say
        // "Minggu"; PRD §7.1 requires "Ahad" for this app's calendar.
        assertEquals("Ahad", HijriCalendarFormatter.formatWeekdayFull(LocalDate.of(2026, 8, 9), WEEKDAY_NAMES))
    }

    @Test
    fun formatWeekdayFullOrdinaryWeekday() {
        assertEquals("Sabtu", HijriCalendarFormatter.formatWeekdayFull(LocalDate.of(2026, 8, 8), WEEKDAY_NAMES))
    }

    @Test
    fun formatWeekdayAndPasaranCombinesAhadWithPasaranName() {
        val text =
            HijriCalendarFormatter.formatWeekdayAndPasaran(
                date = LocalDate.of(2026, 8, 9),
                pasaran = Pasaran.PON,
                weekdayNames = WEEKDAY_NAMES,
                pasaranNames = listOf("Legi", "Pahing", "Pon", "Wage", "Kliwon"),
            )
        assertEquals("Ahad Pon", text)
    }

    @Test
    fun formatHijriMonthSpanSingleMonth() {
        val span = HijriYearMonth(1448, 9)
        assertEquals("Ramadan 1448", HijriCalendarFormatter.formatHijriMonthSpan(span, span, HIJRI_MONTH_NAMES))
    }

    @Test
    fun formatHijriMonthSpanAcrossTwoMonthsSameYear() {
        val start = HijriYearMonth(1448, 2)
        val end = HijriYearMonth(1448, 3)
        assertEquals(
            "Safar – Rabiul Awal 1448",
            HijriCalendarFormatter.formatHijriMonthSpan(start, end, HIJRI_MONTH_NAMES),
        )
    }

    @Test
    fun formatEventHijriRangeForASingleDay() {
        val event = fastingEvent(startDay = 9, endDay = 9, hijriMonth = 12)
        assertEquals("9 Dzulhijjah 1448", HijriCalendarAgendaFormatter.formatEventHijriRange(event, HIJRI_MONTH_NAMES))
    }

    @Test
    fun formatEventHijriRangeForAMultiDayRange() {
        val event = fastingEvent(startDay = 13, endDay = 15, hijriMonth = 3)
        assertEquals(
            "13–15 Rabiul Awal 1448",
            HijriCalendarAgendaFormatter.formatEventHijriRange(event, HIJRI_MONTH_NAMES),
        )
    }

    @Test
    fun formatEventHijriRangeForAFlexibleWindowNeverStatesAHardEndDay() {
        val event = fastingEvent(startDay = 2, endDay = 29, hijriMonth = 10, isFlexibleWindow = true)
        assertEquals(
            "2 s.d. akhir Syawal 1448",
            HijriCalendarAgendaFormatter.formatEventHijriRange(event, HIJRI_MONTH_NAMES),
        )
    }

    @Test
    fun formatAgendaBadgeDayForAMultiDayEvent() {
        val event =
            fastingEvent(startDay = 13, endDay = 15, hijriMonth = 3).copy(
                startDate = LocalDate.of(2026, 8, 26),
                endDate = LocalDate.of(2026, 8, 28),
            )
        assertEquals("26–28", HijriCalendarAgendaFormatter.formatAgendaBadgeDay(event))
    }

    @Test
    fun formatAgendaBadgeDayForASingleDayEvent() {
        val event =
            fastingEvent(startDay = 9, endDay = 9, hijriMonth = 12).copy(
                startDate = LocalDate.of(2026, 5, 26),
                endDate = LocalDate.of(2026, 5, 26),
            )
        assertEquals("26", HijriCalendarAgendaFormatter.formatAgendaBadgeDay(event))
    }

    private fun fastingEvent(
        startDay: Int,
        endDay: Int,
        hijriMonth: Int,
        isFlexibleWindow: Boolean = false,
    ) = HijriCalendarEvent(
        id = "fixture",
        kind = HijriEventKind.FASTING,
        title = "Fixture",
        description = null,
        startDate = LocalDate.of(2026, 1, 1),
        endDate = LocalDate.of(2026, 1, 1),
        hijriYear = 1448,
        hijriMonth = hijriMonth,
        hijriStartDay = startDay,
        hijriEndDay = endDay,
        isFlexibleWindow = isFlexibleWindow,
        calculationStatus = HijriCalculationStatus.UMM_AL_QURA_CALCULATION,
        provenance =
            HijriEventProvenance(
                bundleVersion = 1,
                sourcePublisher = "Fixture",
                sourceTitle = "Fixture",
                sourceUrl = "https://example.invalid",
                sourceYear = null,
                editorialNote = "",
            ),
    )
}
