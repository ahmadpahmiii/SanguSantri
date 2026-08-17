package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.Content

sealed interface SholawatListUiState {
    data object Loading : SholawatListUiState

    data class ContentReady(
        val items: List<Content>,
    ) : SholawatListUiState
}
