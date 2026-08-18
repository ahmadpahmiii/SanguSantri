package com.sangusantri.app.feature.quran.murottal

/** The mini player's controls, bundled so the bar keeps a short parameter list. */
data class QuranMiniPlayerActions(
    val onTogglePlayPause: () -> Unit,
    val onSkipPrevious: () -> Unit,
    val onSkipNext: () -> Unit,
    /** Both the "close" button and "Batal" while an ayah is being prepared. */
    val onClose: () -> Unit,
    val onOpenPanel: () -> Unit,
    val onRetry: () -> Unit,
)
