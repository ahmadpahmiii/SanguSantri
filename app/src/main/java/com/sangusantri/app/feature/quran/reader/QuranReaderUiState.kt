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
        val ayats: List<QuranReaderAyatUiModel>,
        val pages: List<List<QuranReaderAyatUiModel>>,
        /** Latin name of surah [surahNumber] + 1, or `null` on An-Nas. Mushaf mode appends one extra
         * pager page offering it, so swiping past the surah's last page continues reading. */
        val nextSurahName: String?,
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
