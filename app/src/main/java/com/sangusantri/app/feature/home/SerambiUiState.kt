package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content

/** Beranda screen state. [Loaded] with an empty list is a valid state (nothing synced yet). */
sealed interface SerambiUiState {
    data object Loading : SerambiUiState

    data class Loaded(
        val items: List<Content>,
    ) : SerambiUiState
}
