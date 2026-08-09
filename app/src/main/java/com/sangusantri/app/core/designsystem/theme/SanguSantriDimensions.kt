package com.sangusantri.app.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** Shared component dimensions extracted from the approved local design reader exports. */
object SanguSantriDimensions {
    val minimumTouchTarget = 48.dp
    val compactTopAppBarHeight = 56.dp
    val readerContentMaxWidth = 640.dp
    val dashboardContentMaxWidth = 840.dp
    val dashboardGridMinCellWidth = 160.dp
    val dashboardMainFeatureMinCellWidth = 96.dp
    val dashboardSupportingMinCellWidth = 144.dp
    val catalogueGridMinCellWidth = 280.dp
    val readerHorizontalPadding = 20.dp
    val readerCardVerticalPadding = 12.dp
    val readerCardCornerRadius = 22.dp
    val guidedCardCornerRadius = 24.dp
    val guidedCounterWidth = 210.dp
    val guidedCounterHeight = 150.dp
    val overflowMenuWidth = 280.dp
    val readerSheetMaxHeight = 550.dp

    // Standalone Al-Qur'an reader (`0.0.6`).
    val quranSurahHeaderMinHeight = 48.dp

    // design-export/quran/09-flowing-reader-arab-only-page.html `.basmalah img{width:210px}` in a
    // 360dp logical frame.
    val quranBasmalahMaxWidth = 210.dp
    val quranHubContentMaxWidth = 640.dp
    val quranSheetMaxHeight = 610.dp
    val quranSheetCornerRadius = 26.dp
    val quranNoticeCornerRadius = 13.dp
    val quranEmptyStateMarkSize = 58.dp
    val quranEmptyStateDescriptionMaxWidth = 270.dp

    // Shared `.state-mark`/`.button` pattern (design-export/quran) — entry gate and reader's
    // invalid-target state both use this same centred icon-mark + action-button layout.
    val quranStateMarkSize = 76.dp
    val quranStateMarkCornerRadius = 25.dp
    val quranStateActionButtonMinWidth = 132.dp
    val quranEntryProgressWidth = 270.dp

    /** Standalone Tasbih counter (0.0.2) — the strongest visual element on its screen, min 220dp. */
    val tasbihCounterMinSize = 220.dp
}
