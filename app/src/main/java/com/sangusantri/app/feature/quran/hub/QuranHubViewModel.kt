package com.sangusantri.app.feature.quran.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranVerse
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

/**
 * Owns the Quran hub (QUR-FR-005/006/007/011/012): three tabs backed entirely by Room via
 * [QuranRepository], a last-read card, local case/diacritic-tolerant surah search, and a visible
 * non-blocking background refresh status (QUR-FR-004 §6.2).
 */
@HiltViewModel
class QuranHubViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
) : ViewModel() {
    private val selectedTab = MutableStateFlow(QuranHubTab.SURAH)
    private val searchQuery = MutableStateFlow("")
    private val refreshState = MutableStateFlow(QuranHubRefreshState.IDLE)

    private val hubData: Flow<QuranHubData> =
        combine(
            quranRepository.observeSurahs(),
            quranRepository.observeJuzStarts(),
            quranRepository.observeBookmarks(),
            quranRepository.observeReadingState(),
        ) { surahs, juzStarts, bookmarks, readingState ->
            QuranHubData(surahs, juzStarts, bookmarks, readingState)
        }

    val uiState: StateFlow<QuranHubUiState> =
        combine(hubData, selectedTab, searchQuery, refreshState, ::buildUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), QuranHubUiState())

    init {
        viewModelScope.launch {
            var refreshStarted = false
            refreshState.value =
                when (
                    quranRepository.refreshIfStale {
                        refreshStarted = true
                        refreshState.value = QuranHubRefreshState.REFRESHING
                    }
                ) {
                    QuranPreparationResult.Ready -> QuranHubRefreshState.IDLE
                    is QuranPreparationResult.Failed ->
                        if (refreshStarted) QuranHubRefreshState.FAILED else QuranHubRefreshState.IDLE
                }
        }
    }

    fun selectTab(tab: QuranHubTab) {
        selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    private fun buildUiState(
        data: QuranHubData,
        tab: QuranHubTab,
        query: String,
        refresh: QuranHubRefreshState,
    ): QuranHubUiState {
        val surahNames = data.surahs.associate { it.number to it.latinName }
        return QuranHubUiState(
            selectedTab = tab,
            searchQuery = query,
            surahs = data.surahs.filteredBySearch(query),
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
            refreshState = refresh,
        )
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
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
