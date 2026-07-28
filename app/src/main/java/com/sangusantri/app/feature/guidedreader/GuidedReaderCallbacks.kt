package com.sangusantri.app.feature.guidedreader

import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.feature.reader.ReaderUiAction

/** Callbacks shared by the stateless Guided Reader screen and its overlays. */
data class GuidedReaderCallbacks(
    val onAction: (GuidedReaderUiAction) -> Unit,
    val onSettingsAction: (ReaderUiAction) -> Unit,
    val onSetProgressionMode: (GuidedProgressionMode) -> Unit,
    val onBack: () -> Unit,
)
