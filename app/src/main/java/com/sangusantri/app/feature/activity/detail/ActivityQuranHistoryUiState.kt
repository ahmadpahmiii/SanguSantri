package com.sangusantri.app.feature.activity.detail

import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.domain.model.QuranActivityEntry

data class ActivityQuranHistoryUiState(
    val filter: TimeRangeFilter = TimeRangeFilter.ALL,
    val entries: List<QuranActivityEntry> = emptyList(),
)
