package com.sangusantri.app.feature.activity.detail

import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent

data class ActivityAmaliyahHistoryUiState(
    val filter: TimeRangeFilter = TimeRangeFilter.ALL,
    val events: List<AmaliyahCompletionEvent> = emptyList(),
)
