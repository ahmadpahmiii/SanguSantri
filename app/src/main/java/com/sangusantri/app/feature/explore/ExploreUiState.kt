package com.sangusantri.app.feature.explore

import com.sangusantri.app.domain.model.Content

sealed interface ExploreUiState {
    data object Loading : ExploreUiState

    data class ContentReady(
        val items: List<Content>,
        val filteredItems: List<Content>,
        val categories: List<String>,
        val query: String,
        val selectedCategory: String?,
    ) : ExploreUiState
}
