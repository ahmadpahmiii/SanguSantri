package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sangusantri.app.domain.model.QuranDisplayMode

/**
 * Opens on [targetAyat], then maps the outgoing mode's visible ayat to the destination mode.
 *
 * The two modes navigate by different units — mushaf mode turns whole Kemenag `halaman` pages
 * horizontally, translation mode scrolls a continuous ayat list — so they keep separate state and the
 * ayat number is what travels between them. Switching modes therefore lands on the same place in the
 * surah rather than the same scroll offset.
 */
@Composable
internal fun QuranReaderSynchronizePosition(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    mushafPagerState: PagerState,
    translationListState: LazyListState,
): Boolean {
    var previousMode by remember(state.surahNumber) { mutableStateOf<QuranDisplayMode?>(null) }
    LaunchedEffect(state.surahNumber, targetAyat, state.displayMode) {
        val oldMode = previousMode
        val anchorAyat =
            if (oldMode == null) {
                restoredOrRequestedAyat(state, targetAyat, mushafPagerState, translationListState)
            } else {
                visibleAyatForMode(oldMode, state, mushafPagerState, translationListState)
            }
        when (state.displayMode) {
            QuranDisplayMode.ARAB_ONLY -> {
                val page = state.pages.indexOfFirst { page -> page.any { it.ayatNumber == anchorAyat } }
                if (page >= 0) mushafPagerState.scrollToPage(page)
            }

            QuranDisplayMode.ARAB_TRANSLATION -> {
                val index = state.ayats.indexOfFirst { it.ayatNumber == anchorAyat }
                // +1 for the surah-start header, which occupies index 0 of the translation list.
                if (index >= 0) translationListState.scrollToItem(index + 1)
            }
        }
        previousMode = state.displayMode
    }
    return previousMode == state.displayMode
}

/** On first composition there is no outgoing mode: a restored position wins over the requested ayat,
 * so returning to the reader resumes where it was left rather than jumping back to the entry point. */
private fun restoredOrRequestedAyat(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    mushafPagerState: PagerState,
    translationListState: LazyListState,
): Int? {
    val restored =
        when (state.displayMode) {
            QuranDisplayMode.ARAB_ONLY -> mushafPagerState.currentPage > 0
            QuranDisplayMode.ARAB_TRANSLATION ->
                translationListState.firstVisibleItemIndex > 0 ||
                    translationListState.firstVisibleItemScrollOffset > 0
        }
    return if (restored) {
        visibleAyatForMode(state.displayMode, state, mushafPagerState, translationListState)
    } else {
        targetAyat
    }
}

private fun visibleAyatForMode(
    mode: QuranDisplayMode,
    state: QuranReaderUiState.Content,
    mushafPagerState: PagerState,
    translationListState: LazyListState,
): Int? =
    when (mode) {
        QuranDisplayMode.ARAB_ONLY ->
            state.pages
                .getOrNull(mushafPagerState.currentPage)
                ?.firstOrNull()
                ?.ayatNumber

        QuranDisplayMode.ARAB_TRANSLATION ->
            state.ayats
                .getOrNull((translationListState.firstVisibleItemIndex - 1).coerceAtLeast(0))
                ?.ayatNumber
    }
