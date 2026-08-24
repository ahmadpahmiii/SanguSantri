package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.Content

sealed interface SholawatListUiState {
    data object Loading : SholawatListUiState

    data class ContentReady(
        /** Already filtered by [query]. */
        val items: List<Content>,
        val query: String = "",
    ) : SholawatListUiState
}
