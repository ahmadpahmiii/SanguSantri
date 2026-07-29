package com.sangusantri.app.feature.activity

import com.sangusantri.app.domain.model.ActivityOverview

sealed interface ActivityUiState {
    data object Loading : ActivityUiState

    data class Content(
        val overview: ActivityOverview,
    ) : ActivityUiState
}
