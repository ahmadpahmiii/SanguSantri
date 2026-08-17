package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.ContentStep

sealed interface SholawatReaderUiState {
    data object Loading : SholawatReaderUiState

    data object Unavailable : SholawatReaderUiState

    data object RecoverableError : SholawatReaderUiState

    data class ContentAvailable(
        val title: String,
        val steps: List<ContentStep>,
    ) : SholawatReaderUiState
}
