package com.sangusantri.app.feature.quran.hub

import com.sangusantri.app.domain.model.QuranMurottalState
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

/** A surah row's audio state (`4d`). The trailing control and the row subtitle must both say
 * *audio* — a bare download icon reads as "download the surah text", which is already stored. */
enum class QuranSurahAudioState {
    NONE,
    DOWNLOADING,
    PARTIAL,
    STORED,
}

data class QuranHubUiState(
    val selectedTab: QuranHubTab = QuranHubTab.SURAH,
    val searchQuery: String = "",
    val surahs: List<QuranSurah> = emptyList(),
    /** Audio state per surah number; absent means [QuranSurahAudioState.NONE]. */
    val surahAudioStates: Map<Int, QuranSurahAudioState> = emptyMap(),
    /** Fraction `0f..1f` of the surah currently downloading, if any. */
    val downloadingSurahNumber: Int? = null,
    val downloadingFraction: Float = 0f,
    /** The surah playback will continue into, or `null` at the end of the mushaf or when
     * cross-surah continuation is off — the "Sedang diputar" meta line names it instead of
     * printing a queue length that could never be accurate. */
    val murottalNextSurahName: String? = null,
    val juzRows: List<QuranJuzRow> = emptyList(),
    val bookmarkRows: List<QuranBookmarkRow> = emptyList(),
    val continueReading: QuranContinueReading? = null,
    /** Non-idle playback replaces the "Terakhir dibaca" block with "Sedang diputar" (`4d`). */
    val murottal: QuranMurottalState = QuranMurottalState(),
    /** `true` only until Room's first emission arrives — defaulted `true` so the `stateIn` seed
     * value (before any real data is observed) reads as loading rather than a false "empty"
     * Surah/Juz tab (the entry gate already guarantees a non-empty local dataset by the time the
     * hub is reachable, so this covers only that brief first-subscription window). */
    val isLoading: Boolean = true,
)
