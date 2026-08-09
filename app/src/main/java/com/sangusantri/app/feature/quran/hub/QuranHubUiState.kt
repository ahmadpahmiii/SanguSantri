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

/** The prominent continue-reading action shown when a last position exists (QUR-FR-005/011). */
data class QuranContinueReading(
    val surahNumber: Int,
    val surahName: String,
    val ayatNumber: Int,
    val page: Int,
)

data class QuranHubUiState(
    val selectedTab: QuranHubTab = QuranHubTab.SURAH,
    val searchQuery: String = "",
    val surahs: List<QuranSurah> = emptyList(),
    val juzRows: List<QuranJuzRow> = emptyList(),
    val bookmarkRows: List<QuranBookmarkRow> = emptyList(),
    val continueReading: QuranContinueReading? = null,
    /** `true` only until Room's first emission arrives — defaulted `true` so the `stateIn` seed
     * value (before any real data is observed) reads as loading rather than a false "empty"
     * Surah/Juz tab (the entry gate already guarantees a non-empty local dataset by the time the
     * hub is reachable, so this covers only that brief first-subscription window). */
    val isLoading: Boolean = true,
)
