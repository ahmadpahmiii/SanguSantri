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
    /** Per-surah audio download from a surah row's trailing control (`4d`). */
    val onDownloadSurahAudio: (surahNumber: Int) -> Unit,
    val onCancelSurahAudioDownload: () -> Unit,
    /** Plays a fully downloaded surah from ayat 1, straight from the hub list. */
    val onPlaySurahAudio: (surahNumber: Int) -> Unit,
    val onTogglePlayPause: () -> Unit,
    val onSkipPrevious: () -> Unit,
    val onSkipNext: () -> Unit,
)
