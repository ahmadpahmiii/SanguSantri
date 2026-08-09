package com.sangusantri.app.feature.quran.hub

/** [QuranHubScreen]'s parameter-less-ish navigation/interaction actions, bundled to keep the
 * composable's own parameter list short (mirrors `feature/home/SerambiActions`). */
data class QuranHubActions(
    val onBack: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenSource: () -> Unit,
    val onTabSelected: (QuranHubTab) -> Unit,
    val onSearchQueryChanged: (String) -> Unit,
    val onSurahSelected: (surahNumber: Int) -> Unit,
    val onAyatSelected: (surahNumber: Int, ayatNumber: Int) -> Unit,
)
