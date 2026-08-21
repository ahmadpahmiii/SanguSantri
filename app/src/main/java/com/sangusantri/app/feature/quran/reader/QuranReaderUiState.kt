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
        /** Surah [surahNumber] + 1's first page, or `null` on An-Nas. */
        val nextBoundaryPage: QuranBoundaryPage?,
        /** Surah [surahNumber] - 1's last page, or `null` on Al-Fatihah. */
        val previousBoundaryPage: QuranBoundaryPage?,
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

/**
 * One real page of the surah either side of this one, carried so mushaf mode can render it inside the
 * pager at the boundary.
 *
 * Without it, crossing a surah boundary meant swiping onto a blank page and swapping the whole
 * destination underneath the reader's thumb — a hard cut mid-gesture. Drawing the adjacent surah's
 * actual page makes the crossing look like any other page turn, and lets the navigation wait until
 * the pager has settled, at which point the reader is already looking at the page it will land on.
 */
data class QuranBoundaryPage(
    val surahNumber: Int,
    val surahName: String,
    val surahArabicName: String,
    val category: String,
    val ayatCount: Int,
    val ayats: List<QuranReaderAyatUiModel>,
    /** `true` when this page is that surah's first, so it draws the surah-start header here exactly as
     * it will once the reader actually opens there — otherwise the header would pop in on landing. */
    val showsSurahHeader: Boolean,
) {
    /** The ayat the reader opens that surah on, so it lands on this very page rather than its far end. */
    val targetAyat: Int? get() = ayats.firstOrNull()?.ayatNumber
}
