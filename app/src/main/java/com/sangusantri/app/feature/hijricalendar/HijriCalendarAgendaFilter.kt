package com.sangusantri.app.feature.hijricalendar

import com.sangusantri.app.domain.model.HijriCalendarEvent
import com.sangusantri.app.domain.model.HijriEventKind

/** The two non-"Semua" agenda filters (§7.3): [FASTING] keeps only fasting recommendations;
 * [HOLIDAY] keeps everything else — fasting prohibitions, observances, and holidays alike. */
enum class HijriCalendarAgendaFilter {
    ALL,
    FASTING,
    HOLIDAY,
}

/** Pure filter application — no Compose/coroutine dependency, same shape as
 * [com.sangusantri.app.core.designsystem.component.filterByTimeRange]. */
fun List<HijriCalendarEvent>.filterByAgendaFilter(filter: HijriCalendarAgendaFilter): List<HijriCalendarEvent> =
    when (filter) {
        HijriCalendarAgendaFilter.ALL -> this
        HijriCalendarAgendaFilter.FASTING -> filter { it.kind == HijriEventKind.FASTING }
        HijriCalendarAgendaFilter.HOLIDAY -> filter { it.kind != HijriEventKind.FASTING }
    }
