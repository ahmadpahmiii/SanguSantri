package com.sangusantri.app.feature.quran.reader

import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranSurahHeaderVariant

sealed interface QuranReaderUiState {
    data object Loading : QuranReaderUiState

    /** The requested surah does not exist in the locally prepared dataset (an invalid/deleted
     * navigation target). */
    data object Unavailable : QuranReaderUiState

    data class Content(
        val surahNumber: Int,
        val surahName: String,
        val surahArabicName: String,
        /** Al-Fatihah ayat 1 as stored — the tenang header's basmalah. Blank when unavailable. */
        val basmalahArabic: String,
        val surahHeaderVariant: QuranSurahHeaderVariant,
        val category: String,
        val ayatCount: Int,
        val displayMode: QuranDisplayMode,
        val arabicFont: QuranArabicFont,
        val ayats: List<QuranReaderAyatUiModel>,
        val pages: List<List<QuranReaderAyatUiModel>>,
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
