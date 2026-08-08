package com.sangusantri.app.feature.quran.reader

/**
 * Reader-facing projection of one already validated, locally ordered Kemenag ayat.
 *
 * This is deliberately a UI boundary model: Room/network entities must not reach Compose, while
 * the reader needs only stable identity, exact source strings, and source position metadata.
 */
data class QuranReaderAyatUiModel(
    val remoteId: Long,
    val surahName: String,
    val ayatNumber: Int,
    val juz: Int,
    val page: Int,
    val arabicText: String,
    val translation: String,
)
