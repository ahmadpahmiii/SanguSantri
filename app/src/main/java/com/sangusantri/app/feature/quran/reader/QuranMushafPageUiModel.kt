package com.sangusantri.app.feature.quran.reader

/**
 * One mushaf halaman, exactly as the Kemenag dataset prints it.
 *
 * A halaman is a page of the mushaf, not a slice of one surah. Page 603 carries the whole of
 * Al-Kafirun, An-Nasr and Al-Lahab; page 2 carries only the opening of Al-Baqarah. Grouping the
 * page's ayat by surah is the official mapping — every ayat already states its own `halaman`, so
 * nothing here is invented, hardcoded, or inferred.
 */
data class QuranMushafPageUiModel(
    val page: Int,
    val juz: Int,
    val segments: List<QuranMushafSegment>,
)

/** The run of one surah's ayat that falls on a single halaman. */
data class QuranMushafSegment(
    val surahNumber: Int,
    val surahName: String,
    val surahArabicName: String,
    val category: String,
    val ayatCount: Int,
    /**
     * `true` when this run opens the surah, so the page prints its header and basmalah before the
     * Arabic — which is why three surah headers appear part-way down page 603, and none on page 3.
     */
    val startsSurah: Boolean,
    val ayats: List<QuranReaderAyatUiModel>,
)

/** Mushaf pages are numbered 1..604; the pager indexes them from zero. */
const val QURAN_MUSHAF_PAGE_COUNT = 604
