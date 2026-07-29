package com.sangusantri.app.core.designsystem.component

/** Bundles [TimeRangeFilterChips]' selection + callback so consumers with several other parameters
 * (e.g. `ActivityHistoryDetailScaffold`) stay under the parameter-count limit. */
data class TimeRangeFilterState(
    val selected: TimeRangeFilter,
    val onSelect: (TimeRangeFilter) -> Unit,
)
