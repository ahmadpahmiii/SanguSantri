package com.sangusantri.app.feature.activity.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.core.designsystem.component.filterByTimeRange
import com.sangusantri.app.domain.repository.TasbihRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * "Lihat semua" for Aktivitas' (0.0.3) tasbih-history section — reuses [TasbihRepository]'s
 * existing (0.0.2) history data directly, not a duplicate model. Distinct from Tasbih's own
 * unfiltered `TasbihHistoryScreen` (reached from the Tasbih tab) — this screen adds the filter
 * capability Aktivitas' own spec calls for.
 */
@HiltViewModel
class ActivityTasbihHistoryViewModel
    @Inject
    constructor(
        tasbihRepository: TasbihRepository,
    ) : ViewModel() {
        private val filter = MutableStateFlow(TimeRangeFilter.ALL)

        val uiState: StateFlow<ActivityTasbihHistoryUiState> =
            combine(tasbihRepository.observeHistory(), filter) { entries, range ->
                ActivityTasbihHistoryUiState(
                    filter = range,
                    entries = entries.filterByTimeRange(range, System.currentTimeMillis()) { it.endedAtEpochMillis },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = ActivityTasbihHistoryUiState(),
            )

        fun onFilterSelected(range: TimeRangeFilter) {
            filter.value = range
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
