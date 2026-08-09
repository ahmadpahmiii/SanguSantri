package com.sangusantri.app.feature.hijricalendar

import com.sangusantri.app.domain.model.HijriCalendarDay
import com.sangusantri.app.domain.model.HijriCalendarEvent
import com.sangusantri.app.domain.model.HijriCalendarMonth
import java.time.LocalDate

/**
 * Kalender Hijriah's single screen state (§9). There is no loading/offline-error state — every
 * value here is computed locally and synchronously (PRD §9: "no loading/offline-error state for
 * the core calendar because all required data is local").
 */
data class HijriCalendarUiState(
    val month: HijriCalendarMonth,
    val selectedDate: LocalDate,
    val agendaEvents: List<HijriCalendarEvent>,
    val agendaFilter: HijriCalendarAgendaFilter,
    val isSourceSheetVisible: Boolean,
    val canGoToPreviousMonth: Boolean,
    val canGoToNextMonth: Boolean,
    /** The agenda row whose provenance detail is open (CAL-FR-008) — `null` when none is. */
    val detailEventId: String? = null,
) {
    /** Falls back to the first current-month day if [selectedDate] briefly falls outside [month]'s
     * grid — never null, since the design always shows exactly one selected date. */
    val selectedDay: HijriCalendarDay
        get() = month.days.firstOrNull { it.date == selectedDate } ?: month.days.first { it.isCurrentMonth }

    val filteredAgendaEvents: List<HijriCalendarEvent>
        get() = agendaEvents.filterByAgendaFilter(agendaFilter)

    val detailEvent: HijriCalendarEvent?
        get() = agendaEvents.find { it.id == detailEventId }
}
