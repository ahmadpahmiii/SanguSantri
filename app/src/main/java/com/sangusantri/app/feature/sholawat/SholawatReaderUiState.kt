package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.QuranArabicFont

sealed interface SholawatReaderUiState {
    data object Loading : SholawatReaderUiState

    data object Unavailable : SholawatReaderUiState

    data object RecoverableError : SholawatReaderUiState

    data class ContentAvailable(
        val title: String,
        val steps: List<ContentStep>,
        /** The app-wide Arabic typeface chosen in the Quran reader's settings, applied here too. */
        val arabicFont: QuranArabicFont,
    ) : SholawatReaderUiState
}
