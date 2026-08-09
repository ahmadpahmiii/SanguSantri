package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sangusantri.app.domain.model.QuranDisplayMode

/**
 * Opens on [targetAyat], then maps the outgoing mode's first visible ayat to the destination mode.
 * Separate list states let the mode crossfade compose both layouts without scroll contention.
 */
@Composable
internal fun QuranReaderSynchronizePosition(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    arabOnlyListState: LazyListState,
    translationListState: LazyListState,
): Boolean {
    var previousMode by remember(state.surahNumber) { mutableStateOf<QuranDisplayMode?>(null) }
    LaunchedEffect(state.surahNumber, targetAyat, state.displayMode) {
        val oldMode = previousMode
        val anchorAyat =
            if (oldMode == null) {
                restoredOrRequestedAyat(state, targetAyat, arabOnlyListState, translationListState)
            } else {
                visibleAyatForMode(
                    mode = oldMode,
                    firstVisibleItemIndex =
                        listStateForMode(oldMode, arabOnlyListState, translationListState).firstVisibleItemIndex,
                    state = state,
                )
            }
        val destinationState = listStateForMode(state.displayMode, arabOnlyListState, translationListState)
        val destinationIndex = itemIndexForAyat(state.displayMode, anchorAyat, state)
        if (destinationIndex != null) destinationState.scrollToItem(destinationIndex)
        previousMode = state.displayMode
    }
    return previousMode == state.displayMode
}

private fun restoredOrRequestedAyat(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    arabOnlyListState: LazyListState,
    translationListState: LazyListState,
): Int? {
    val restoredState = listStateForMode(state.displayMode, arabOnlyListState, translationListState)
    return if (restoredState.firstVisibleItemIndex > 0 || restoredState.firstVisibleItemScrollOffset > 0) {
        visibleAyatForMode(state.displayMode, restoredState.firstVisibleItemIndex, state)
    } else {
        targetAyat
    }
}

private fun listStateForMode(
    mode: QuranDisplayMode,
    arabOnlyListState: LazyListState,
    translationListState: LazyListState,
): LazyListState =
    when (mode) {
        QuranDisplayMode.ARAB_ONLY -> arabOnlyListState
        QuranDisplayMode.ARAB_TRANSLATION -> translationListState
    }

private fun visibleAyatForMode(
    mode: QuranDisplayMode,
    firstVisibleItemIndex: Int,
    state: QuranReaderUiState.Content,
): Int? {
    val contentIndex = (firstVisibleItemIndex - 1).coerceAtLeast(0)
    return when (mode) {
        QuranDisplayMode.ARAB_TRANSLATION -> state.ayats.getOrNull(contentIndex)?.ayatNumber
        QuranDisplayMode.ARAB_ONLY ->
            state.pages
                .getOrNull(contentIndex)
                ?.firstOrNull()
                ?.ayatNumber
    }
}

private fun itemIndexForAyat(
    mode: QuranDisplayMode,
    ayatNumber: Int?,
    state: QuranReaderUiState.Content,
): Int? =
    when (mode) {
        QuranDisplayMode.ARAB_TRANSLATION -> state.ayats.indexOfFirst { it.ayatNumber == ayatNumber }
        QuranDisplayMode.ARAB_ONLY -> state.pages.indexOfFirst { page -> page.any { it.ayatNumber == ayatNumber } }
    }.takeIf { it >= 0 }?.plus(1)
