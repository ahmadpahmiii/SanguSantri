package com.sangusantri.app.feature.quran.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.audio.QuranAudioDownloadManager
import com.sangusantri.app.data.audio.QuranAudioStore
import com.sangusantri.app.data.audio.QuranMurottalPlayer
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.QuranAudioDownloadProgress
import com.sangusantri.app.domain.model.QuranAudioLibrary
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject

private data class QuranHubData(
    val surahs: List<QuranSurah>,
    val juzStarts: List<QuranVerse>,
    val bookmarks: List<QuranBookmark>,
    val readingState: QuranReadingState?,
)

/** Audio-side inputs, combined separately from [QuranHubData] so Room's four flows and the three
 * playback flows stay under `combine`'s five-argument typed overloads. */
private data class QuranHubAudioData(
    val murottal: QuranMurottalState,
    val library: QuranAudioLibrary,
    val download: QuranAudioDownloadProgress?,
)

/**
 * Owns the Quran hub (QUR-FR-005/006/007/011/012): three tabs backed entirely by Room via
 * [QuranRepository], a last-read card, and local case/diacritic-tolerant surah search. Corpus
 * update scheduling is application-owned and never a hub/ViewModel network side effect.
 */
// Three tab/search setters, three transport controls, the per-surah download actions and the theme
// toggle — each one a distinct thing the hub's UI can do, with no shared logic to fold them into.
// Same reasoning as [QuranRepository]'s own suppression.
@Suppress("TooManyFunctions")
@HiltViewModel
class QuranHubViewModel
@Inject
constructor(
    private val quranRepository: QuranRepository,
    private val settingsRepository: QuranReaderSettingsRepository,
    private val murottalPlayer: QuranMurottalPlayer,
    private val audioStore: QuranAudioStore,
    private val audioDownloadManager: QuranAudioDownloadManager,
) : ViewModel() {
    private val selectedTab = MutableStateFlow(QuranHubTab.SURAH)
    private val searchQuery = MutableStateFlow("")

    private val hubData: Flow<QuranHubData> =
        combine(
            quranRepository.observeSurahs(),
            quranRepository.observeJuzStarts(),
            quranRepository.observeBookmarks(),
            quranRepository.observeReadingState(),
        ) { surahs, juzStarts, bookmarks, readingState ->
            QuranHubData(surahs, juzStarts, bookmarks, readingState)
        }

    private val audioData: Flow<QuranHubAudioData> =
        combine(
            murottalPlayer.state,
            audioStore.library,
            audioDownloadManager.progress,
        ) { murottal, library, download ->
            QuranHubAudioData(murottal, library, download)
        }

    val uiState: StateFlow<QuranHubUiState> =
        combine(hubData, audioData, selectedTab, searchQuery, ::buildUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), QuranHubUiState())

    init {
        audioStore.ensureDirectory()
        viewModelScope.launch { audioStore.refresh() }
    }

    fun selectTab(tab: QuranHubTab) {
        selectedTab.value = tab
    }

    fun downloadSurahAudio(surahNumber: Int) {
        val ayatCount =
            uiState.value.surahs
                .firstOrNull { it.number == surahNumber }
                ?.ayatCount ?: return
        audioDownloadManager.start(surahNumber, ayatCount)
    }

    fun cancelSurahAudioDownload() = audioDownloadManager.cancel()

    /** Starts a stored surah from its first ayah without opening the reader. Every ayah is already on
     * disk when this control is offered, so nothing is fetched. */
    fun playSurahAudio(surahNumber: Int) = murottalPlayer.play(surahNumber, ayahNumber = 1)

    fun togglePlayPause() = murottalPlayer.togglePlayPause()

    fun skipPrevious() = murottalPlayer.skipToPrevious()

    fun skipNext() = murottalPlayer.skipToNext()

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    private fun buildUiState(
        data: QuranHubData,
        audio: QuranHubAudioData,
        tab: QuranHubTab,
        query: String,
    ): QuranHubUiState {
        val surahNames = data.surahs.associate { it.number to it.latinName }
        return QuranHubUiState(
            selectedTab = tab,
            searchQuery = query,
            surahs = data.surahs.filteredBySearch(query),
            surahAudioStates = data.surahs.associate { it.number to audio.audioStateOf(it) },
            downloadingSurahNumber = audio.download?.surahNumber,
            downloadingFraction = audio.download?.fraction ?: 0f,
            // The player's queue preview starts with whatever comes next, and is already empty when
            // cross-surah continuation is off or this is the last surah.
            murottalNextSurahName = audio.murottal.queuedSurahNames.firstOrNull(),
            murottal = audio.murottal,
            juzRows =
                data.juzStarts.map { verse ->
                    QuranJuzRow(
                        juzNumber = verse.juz,
                        surahNumber = verse.surahNumber,
                        surahName = surahNames[verse.surahNumber].orEmpty(),
                        ayatNumber = verse.ayatNumber,
                        page = verse.page,
                    )
                },
            bookmarkRows =
                data.bookmarks.map { bookmark ->
                    QuranBookmarkRow(
                        surahNumber = bookmark.surahNumber,
                        surahName = surahNames[bookmark.surahNumber].orEmpty(),
                        ayatNumber = bookmark.ayatNumber,
                        createdAtEpochMillis = bookmark.createdAtEpochMillis,
                    )
                },
            continueReading =
                data.readingState?.let { state ->
                    QuranContinueReading(
                        surahNumber = state.surahNumber,
                        surahName = surahNames[state.surahNumber].orEmpty(),
                        ayatNumber = state.ayatNumber,
                        page = state.page,
                    )
                },
            isLoading = false,
        )
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
}

/** A surah's trailing audio state. Partial coverage is deliberately not reported as stored — a
 * half-downloaded surah must not advertise itself as playable offline. */
private fun QuranHubAudioData.audioStateOf(surah: QuranSurah): QuranSurahAudioState =
    when {
        download?.surahNumber == surah.number -> QuranSurahAudioState.DOWNLOADING
        library.isSurahComplete(surah.number, surah.ayatCount) -> QuranSurahAudioState.STORED
        library.storedAyahCount(surah.number) > 0 -> QuranSurahAudioState.PARTIAL
        else -> QuranSurahAudioState.NONE
    }

/** Case/diacritic-tolerant Latin-name match, or an exact surah-number match (QUR-FR-006) — local
 * only, never a network request. */
private fun List<QuranSurah>.filteredBySearch(query: String): List<QuranSurah> {
    if (query.isBlank()) return this
    val normalizedQuery = query.normalizedForSearch()
    val numberQuery = query.toIntOrNull()
    return filter { surah ->
        surah.latinName.normalizedForSearch().contains(normalizedQuery) ||
            surah.number == numberQuery
    }
}

private fun String.normalizedForSearch(): String =
    Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace(DIACRITIC_MARK_REGEX, "")
        .lowercase(Locale.ROOT)

private val DIACRITIC_MARK_REGEX = Regex("\\p{Mn}+")
