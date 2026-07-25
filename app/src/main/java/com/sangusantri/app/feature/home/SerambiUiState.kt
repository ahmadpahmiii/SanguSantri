package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Amaliyah

/** Serambi screen state. [Content] with an empty list is a valid state (nothing seeded yet). */
sealed interface SerambiUiState {
    data object Loading : SerambiUiState

    data class Content(
        val amaliyah: List<Amaliyah>,
    ) : SerambiUiState
}
