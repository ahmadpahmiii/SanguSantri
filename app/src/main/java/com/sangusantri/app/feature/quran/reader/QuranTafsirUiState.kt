package com.sangusantri.app.feature.quran.reader

import com.sangusantri.app.domain.model.QuranTafsir

/** The tafsir bottom sheet's state (QUR-FR-013). `null` (owned by [QuranReaderViewModel], not
 * modeled here) means the sheet is closed. */
sealed interface QuranTafsirUiState {
    /** No cache yet — the very first fetch for this ayat is in flight. */
    data object Loading : QuranTafsirUiState

    data class Loaded(
        val tafsir: QuranTafsir,
        val isRefreshing: Boolean,
    ) : QuranTafsirUiState

    /** No cache, and the fetch failed. */
    data class Unavailable(
        val retryable: Boolean,
    ) : QuranTafsirUiState
}
