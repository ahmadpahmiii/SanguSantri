package com.sangusantri.app.feature.activity.detail

import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.domain.model.TasbihHistoryEntry

data class ActivityTasbihHistoryUiState(
    val filter: TimeRangeFilter = TimeRangeFilter.ALL,
    val entries: List<TasbihHistoryEntry> = emptyList(),
)
