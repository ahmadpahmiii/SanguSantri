package com.sangusantri.app.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.domain.repository.AmalanRepository
import com.sangusantri.app.domain.usecase.ObserveActivityOverviewUseCase
import com.sangusantri.app.domain.usecase.ObserveAmalanHarianUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel
@Inject
constructor(
    observeActivityOverview: ObserveActivityOverviewUseCase,
    observeAmalanHarian: ObserveAmalanHarianUseCase,
    private val amalanRepository: AmalanRepository,
) : ViewModel() {
    val uiState: StateFlow<ActivityUiState> =
        combine(
            observeActivityOverview(),
            observeAmalanHarian(),
            amalanRepository.observeCelebratedMilestones(),
        ) { overview, amalan, celebrated ->
            ActivityUiState.Content(
                overview = overview,
                amalan = amalan,
                pendingMilestone = amalan.pendingMilestone(celebrated),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ActivityUiState.Loading,
        )

    fun setUdzur(active: Boolean) {
        viewModelScope.launch { amalanRepository.setUdzurActive(active) }
    }

    /** Marks every milestone up to [streakDays], so a streak that passed two of them while the
     * app was closed shows one sheet, not two in a row. */
    fun dismissMilestone(streakDays: Int) {
        viewModelScope.launch {
            AmalanHarian.MILESTONES
                .filter { it <= streakDays }
                .forEach { amalanRepository.markMilestoneCelebrated(it) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
