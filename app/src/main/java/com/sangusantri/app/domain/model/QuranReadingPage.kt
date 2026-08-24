package com.sangusantri.app.domain.model

/**
 * One mushaf page touched by one reading session — the raw input to Amalan Harian's "3 halaman"
 * target ([AmalanHarian.QURAN_PAGE_TARGET]).
 *
 * A session records an ayat range, not pages, so the page comes from the Kemenag dataset's own
 * `page` column rather than an estimate: three halaman means three printed pages, counted
 * distinctly per day.
 */
data class QuranReadingPage(
    val readAtEpochMillis: Long,
    val page: Int,
)
