package com.sangusantri.app.feature.guidedreader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Bundles [GuidedReaderScreen]'s bottom-sheet/dialog visibility flags, under the parameter-count limit. */
internal data class GuidedReaderOverlayVisibility(
    val showSettings: MutableState<Boolean>,
    val showResetConfirm: MutableState<Boolean>,
    val showCompletionConfirm: MutableState<Boolean>,
)

/** Each flag is individually `rememberSaveable` (survives rotation/process death) — the bundle is a plain grouping. */
@Composable
internal fun rememberGuidedReaderOverlayVisibility(): GuidedReaderOverlayVisibility {
    val showSettings = rememberSaveable { mutableStateOf(false) }
    val showResetConfirm = rememberSaveable { mutableStateOf(false) }
    val showCompletionConfirm = rememberSaveable { mutableStateOf(false) }
    return remember(showSettings, showResetConfirm, showCompletionConfirm) {
        GuidedReaderOverlayVisibility(showSettings, showResetConfirm, showCompletionConfirm)
    }
}

/** One-shot flag: shows the saved-position pill only once, if the reader actually resumed mid-way. */
@Composable
internal fun rememberInitialSavedPositionFlag(uiState: GuidedReaderUiState): Boolean {
    var initialPositionChecked by rememberSaveable { mutableStateOf(false) }
    var showSavedPosition by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val state = uiState
        if (!initialPositionChecked && state is GuidedReaderUiState.StepVisible) {
            initialPositionChecked = true
            if (state.stepIndex > 0) showSavedPosition = true
        }
    }
    return showSavedPosition
}
