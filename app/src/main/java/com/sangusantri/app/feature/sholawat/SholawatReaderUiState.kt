package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.ContentLayout
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderSettings

sealed interface SholawatReaderUiState {
    data object Loading : SholawatReaderUiState

    data object Unavailable : SholawatReaderUiState

    data object RecoverableError : SholawatReaderUiState

    data class ContentAvailable(
        val title: String,
        val steps: List<ContentStep>,
        /**
         * How the CMS says these steps are arranged. The reader cannot infer it: paired verse and
         * continuous prose look identical in the text, and pairing prose splits its sentences
         * across two columns.
         */
        val layout: ContentLayout,
        /**
         * The shared reader preferences — sizes, spacing, translation visibility, and the two
         * Sholawat-only layout switches. `arabicFont` rides along inside it, merged from the
         * app-wide Quran setting by the ViewModel exactly as the Full and Guided Readers do.
         */
        val settings: ReaderSettings,
    ) : SholawatReaderUiState
}
