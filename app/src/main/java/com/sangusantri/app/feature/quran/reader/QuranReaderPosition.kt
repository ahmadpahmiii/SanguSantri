package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sangusantri.app.domain.model.QuranDisplayMode

/**
 * Carries the reading position across a change of display mode.
 *
 * The two modes navigate by different units — mushaf mode turns whole halaman of the printed
 * mushaf, Arab+terjemahan scrolls one surah's ayat — so the ayat number is what travels between
 * them. Only the translation list is placed here: the mushaf pager is positioned from
 * [QuranReaderUiState.Content.currentMushafPage], which the ViewModel owns and keeps current as the
 * reader pages through the mushaf.
 *
 * Keyed on the mode and surah alone, never on the anchor. Keying on the anchor would re-run every
 * time scrolling reported a new visible ayat, and the list would fight the thumb scrolling it.
 */
@Composable
internal fun QuranReaderSynchronizePosition(
    state: QuranReaderUiState.Content,
    anchorAyat: Int?,
    translationListState: LazyListState,
) {
    LaunchedEffect(state.surahNumber, state.displayMode) {
        if (state.displayMode != QuranDisplayMode.ARAB_TRANSLATION) return@LaunchedEffect
        val index = state.ayats.indexOfFirst { it.ayatNumber == anchorAyat }
        // +1 for the surah-start header, which occupies index 0 of the translation list.
        if (index >= 0) translationListState.scrollToItem(index + 1)
    }
}
