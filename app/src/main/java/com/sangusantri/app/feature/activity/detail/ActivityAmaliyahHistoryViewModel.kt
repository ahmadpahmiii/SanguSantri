package com.sangusantri.app.feature.activity.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.core.designsystem.component.filterByTimeRange
import com.sangusantri.app.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** "Lihat semua" for Aktivitas' (0.0.3) amaliyah-completion history — full list, filterable, no cap. */
@HiltViewModel
class ActivityAmaliyahHistoryViewModel
    @Inject
    constructor(
        activityRepository: ActivityRepository,
    ) : ViewModel() {
        private val filter = MutableStateFlow(TimeRangeFilter.ALL)

        val uiState: StateFlow<ActivityAmaliyahHistoryUiState> =
            combine(activityRepository.observeCompletions(), filter) { events, range ->
                ActivityAmaliyahHistoryUiState(
                    filter = range,
                    events = events.filterByTimeRange(range, System.currentTimeMillis()) { it.completedAtEpochMillis },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ActivityAmaliyahHistoryUiState(),
            )

        fun onFilterSelected(range: TimeRangeFilter) {
            filter.value = range
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
