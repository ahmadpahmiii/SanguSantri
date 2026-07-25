package com.sangusantri.app.feature.guidedreader.components

/** Bundles the tasbih's two callbacks so [GuidedStepContent] stays under the parameter-count limit. */
internal data class TasbihActions(
    val onIncrement: () -> Unit,
    val onRequestReset: () -> Unit,
)
