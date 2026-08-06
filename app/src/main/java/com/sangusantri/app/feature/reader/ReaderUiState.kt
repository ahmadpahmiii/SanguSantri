package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderSettings

/** Full Reader screen state (Milestone 3). */
sealed interface ReaderUiState {
    data object Loading : ReaderUiState

    data class ContentAvailable(
        val title: String,
        val contentId: String,
        val steps: List<ContentStep>,
        val settings: ReaderSettings,
        val initialItemIndex: Int,
        val initialItemOffset: Int,
        val sourceName: String,
    ) : ReaderUiState

    /** No content for the id, or it has no steps. */
    data object ContentUnavailable : ReaderUiState

    /** An unexpected load failure the user can retry — never a raw exception message. */
    data object RecoverableError : ReaderUiState
}
