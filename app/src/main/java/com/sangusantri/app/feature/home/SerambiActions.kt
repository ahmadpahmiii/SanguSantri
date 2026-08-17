package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.ReaderMode

/** User actions consumed by Beranda's stateless Compose tree. */
data class SerambiActions(
    val onExploreClick: () -> Unit,
    val onPengingatClick: () -> Unit,
    val onBelajarClick: () -> Unit,
    val onQuranClick: () -> Unit,
    val onContinueAmaliyah: (contentId: String, mode: ReaderMode) -> Unit,
    val onContinueQuran: (surahNumber: Int, ayatNumber: Int) -> Unit,
    val onContinueTasbih: () -> Unit,
    val onDismissResume: (fingerprint: String) -> Unit = {},
    val onHijriCalendarClick: () -> Unit = {},
    val onSholawatClick: () -> Unit = {},
    val onPrayerScheduleClick: () -> Unit = {},
    /** Kiblat has no screen of its own — it lives inside Jadwal Sholat (handoff decision), so this
     * routes to the same destination as [onPrayerScheduleClick]. */
    val onKiblatClick: () -> Unit = {},
    val onThemeModeSelected: (AppThemeMode) -> Unit = {},
)
