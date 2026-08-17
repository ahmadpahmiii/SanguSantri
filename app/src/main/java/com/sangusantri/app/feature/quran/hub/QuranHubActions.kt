package com.sangusantri.app.feature.quran.hub

import com.sangusantri.app.domain.model.AppThemeMode

/** [QuranHubScreen]'s parameter-less-ish navigation/interaction actions, bundled to keep the
 * composable's own parameter list short (mirrors `feature/home/SerambiActions`). */
data class QuranHubActions(
    val onBack: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenSource: () -> Unit,
    val onToggleTheme: (AppThemeMode) -> Unit,
    val onTabSelected: (QuranHubTab) -> Unit,
    val onSearchQueryChanged: (String) -> Unit,
    val onSurahSelected: (surahNumber: Int) -> Unit,
    val onAyatSelected: (surahNumber: Int, ayatNumber: Int) -> Unit,
)
