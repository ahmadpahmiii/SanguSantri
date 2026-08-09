package com.sangusantri.app.feature.hijricalendar

import java.time.LocalDate

sealed interface HijriCalendarUiAction {
    data class SelectDate(
        val date: LocalDate,
    ) : HijriCalendarUiAction

    data object GoToPreviousMonth : HijriCalendarUiAction

    data object GoToNextMonth : HijriCalendarUiAction

    data object GoToToday : HijriCalendarUiAction

    data class SelectFilter(
        val filter: HijriCalendarAgendaFilter,
    ) : HijriCalendarUiAction

    data object OpenSourceSheet : HijriCalendarUiAction

    data object DismissSourceSheet : HijriCalendarUiAction

    data class ShowEventDetail(
        val eventId: String,
    ) : HijriCalendarUiAction

    data object DismissEventDetail : HijriCalendarUiAction
}
