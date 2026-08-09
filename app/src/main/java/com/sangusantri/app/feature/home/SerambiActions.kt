package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.ReaderMode

/** User actions consumed by Beranda's stateless Compose tree. */
data class SerambiActions(
    val onSetelanClick: () -> Unit,
    val onAboutClick: () -> Unit,
    val onExploreClick: () -> Unit,
    val onPengingatClick: () -> Unit,
    val onBelajarClick: () -> Unit,
    val onQuranClick: () -> Unit,
    val onContinueAmaliyah: (contentId: String, mode: ReaderMode) -> Unit,
    val onContinueQuran: (surahNumber: Int, ayatNumber: Int) -> Unit,
    val onContinueTasbih: () -> Unit,
    val onDismissResume: (fingerprint: String) -> Unit = {},
    val onHijriCalendarClick: () -> Unit = {},
)
