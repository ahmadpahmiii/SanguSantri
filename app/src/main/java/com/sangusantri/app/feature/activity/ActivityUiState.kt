package com.sangusantri.app.feature.activity

import com.sangusantri.app.domain.model.ActivityOverview
import com.sangusantri.app.domain.model.AmalanHarian

sealed interface ActivityUiState {
    data object Loading : ActivityUiState

    data class Content(
        val overview: ActivityOverview,
        val amalan: AmalanHarian,
        /** The streak length worth marking right now, or `null` — see `AmalanMilestoneSheet`. */
        val pendingMilestone: Int? = null,
    ) : ActivityUiState
}
