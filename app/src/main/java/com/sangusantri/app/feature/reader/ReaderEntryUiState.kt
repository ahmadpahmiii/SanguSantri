package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.ReaderMode

/**
 * State for the reading-mode gate opened from Beranda (PRD 8.2). Resolves to a remembered
 * [ReaderMode] automatically when one exists; otherwise the user is asked to choose once, and that
 * choice is remembered for next time.
 */
sealed interface ReaderEntryUiState {
    data object Loading : ReaderEntryUiState

    data class ModeChooser(
        val title: String,
    ) : ReaderEntryUiState

    data class Resolved(
        val mode: ReaderMode,
    ) : ReaderEntryUiState

    /** No content for the id, or it has no steps — handled the same as the readers themselves. */
    data object ContentUnavailable : ReaderEntryUiState
}
