package com.sangusantri.app.feature.quran.reader

/**
 * Maps between mushaf pager indices and indices into [QuranReaderUiState.Content.pages].
 *
 * The pager carries up to two pages that are not scripture: a leading previous-surah page and a
 * trailing next-surah page, each present only when that surah exists. That makes "pager page 3" and
 * "the surah's 3rd halaman" different numbers, and confusing the two is how a reader ends up scrolled
 * to the wrong page, or navigated straight back out of the surah on open. Every conversion goes
 * through here rather than being re-derived at each call site.
 */
internal data class QuranMushafPaging(
    val leadingPages: Int,
    val realPageCount: Int,
    val trailingPages: Int,
) {
    val pageCount: Int = leadingPages + realPageCount + trailingPages

    /** The pager index of the surah's own first halaman — where the reader must open. Never 0 when a
     * leading page exists, or it would settle on that page and navigate away immediately. */
    val firstRealPage: Int = leadingPages

    fun contentIndex(pagerIndex: Int): Int = pagerIndex - leadingPages

    fun pagerIndex(contentIndex: Int): Int = contentIndex + leadingPages

    fun isPreviousSurahPage(pagerIndex: Int): Boolean = leadingPages > 0 && pagerIndex == 0

    fun isNextSurahPage(pagerIndex: Int): Boolean = pagerIndex >= leadingPages + realPageCount
}

/**
 * Derived from which surahs *exist*, never from whether their pages have arrived from Room yet.
 *
 * Keying it on data presence made the layout move under the reader: the frame after a surah change
 * has no neighbour verses, so there was no leading page and every real page sat one index lower;
 * when Room answered a moment later the leading page appeared and shifted the whole pager by one,
 * landing on a different page than the one being read. The boundary page is simply blank until its
 * verses load, which nobody can see — reaching it takes a swipe, and the read finishes first.
 */
internal fun QuranReaderUiState.Content.mushafPaging(): QuranMushafPaging =
    QuranMushafPaging(
        leadingPages = if (surahNumber > FIRST_SURAH_NUMBER) 1 else 0,
        realPageCount = pages.size,
        trailingPages = if (surahNumber < LAST_SURAH_NUMBER) 1 else 0,
    )

private const val FIRST_SURAH_NUMBER = 1
private const val LAST_SURAH_NUMBER = 114
