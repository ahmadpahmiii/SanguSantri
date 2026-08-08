package com.sangusantri.app.feature.quran.reader

data class QuranAyatActionSheetActions(
    val onToggleBookmark: () -> Unit,
    val onOpenTafsir: () -> Unit,
    val onMarkLastRead: () -> Unit,
    val onShowPosition: () -> Unit,
)
