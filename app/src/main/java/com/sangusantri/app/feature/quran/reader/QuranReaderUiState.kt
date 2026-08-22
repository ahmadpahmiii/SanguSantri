package com.sangusantri.app.feature.quran.reader

import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode

sealed interface QuranReaderUiState {
    data object Loading : QuranReaderUiState

    /** The requested surah does not exist in the locally prepared dataset (an invalid/deleted
     * navigation target). */
    data object Unavailable : QuranReaderUiState

    data class Content(
        val surahNumber: Int,
        val surahName: String,
        val surahArabicName: String,
        /** Al-Fatihah ayat 1 as stored — the surah header's basmalah. Blank when unavailable. */
        val basmalahArabic: String,
        val category: String,
        val ayatCount: Int,
        val displayMode: QuranDisplayMode,
        val arabicFont: QuranArabicFont,
        /** The active surah's ayat, used by Arab+terjemahan mode, which stays surah-based. */
        val ayats: List<QuranReaderAyatUiModel>,
        /**
         * The mushaf halaman currently loaded, keyed by page number — a sliding window around
         * [currentMushafPage], not all 604. Pages outside it are simply absent and render blank
         * until the window catches up, which takes a swipe to reach.
         */
        val mushafPages: Map<Int, QuranMushafPageUiModel>,
        /** The halaman being read, 1..[QURAN_MUSHAF_PAGE_COUNT]. */
        val currentMushafPage: Int,
        val selectedAyat: QuranReaderAyatUiModel?,
        val isSelectedBookmarked: Boolean,
        /** `true` once "Tafsir Kemenag" is chosen from the action sheet for [selectedAyat] — the
         * screen renders the tafsir sheet instead of the action sheet while this is set. */
        val tafsirSheetOpen: Boolean,
        val arabicSizeSp: Int,
        val arabicLineHeightSp: Int,
        val translationSizeSp: Int,
        val brightnessOverride: Float?,
    ) : QuranReaderUiState
}
