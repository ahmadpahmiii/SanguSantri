package com.sangusantri.app.feature.quran.hub

import com.sangusantri.app.domain.model.QuranSurah

/** One resolved Juz row — the Juz number plus its first locally ordered surah/ayat/page (QUR-FR-007). */
data class QuranJuzRow(
    val juzNumber: Int,
    val surahNumber: Int,
    val surahName: String,
    val ayatNumber: Int,
    val page: Int,
)

/** One resolved bookmark row (QUR-FR-012). */
data class QuranBookmarkRow(
    val surahNumber: Int,
    val surahName: String,
    val ayatNumber: Int,
    val createdAtEpochMillis: Long,
)

/** One resolved reading-session row for the Terakhir Dibaca tab (QUR-FR-017). */
data class QuranRecentSessionRow(
    val surahNumber: Int,
    val surahName: String,
    val startAyat: Int,
    val endAyat: Int,
    val readAtEpochMillis: Long,
)

/** The prominent continue-reading action shown when a last position exists (QUR-FR-005/011). */
data class QuranContinueReading(
    val surahNumber: Int,
    val surahName: String,
    val ayatNumber: Int,
)

data class QuranHubUiState(
    val selectedTab: QuranHubTab = QuranHubTab.SURAH,
    val searchQuery: String = "",
    val surahs: List<QuranSurah> = emptyList(),
    val juzRows: List<QuranJuzRow> = emptyList(),
    val bookmarkRows: List<QuranBookmarkRow> = emptyList(),
    val recentSessionRows: List<QuranRecentSessionRow> = emptyList(),
    val continueReading: QuranContinueReading? = null,
)
