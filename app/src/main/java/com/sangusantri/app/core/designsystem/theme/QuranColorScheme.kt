package com.sangusantri.app.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.sangusantri.app.domain.model.QuranThemeMode

/** The active Quran reading-surface mode (QUR-FR-015 amendment, ADR 0016), provided once by
 * [com.sangusantri.app.navigation.SanguSantriNavHost] from the persisted setting so every Quran
 * screen — and the nav host's own background behind the Quran destination — stays in sync. Falls
 * back to [QuranThemeMode.DARK] (the feature's original default) for any composable rendered
 * outside that provider, e.g. `@Preview`s. */
val LocalQuranThemeMode = compositionLocalOf { QuranThemeMode.DARK }

/**
 * Live-resolved Quran colour roles. Each property keeps the exact name every Quran screen already
 * imports from `Color.kt` before the light/dark split — only the source file changed, so no
 * screen's imports needed touching to gain light-mode support.
 */
val QuranBackground: Color
    @Composable get() = quranColor(QuranBackgroundDark, QuranBackgroundLight)

val QuranSurface: Color
    @Composable get() = quranColor(QuranSurfaceDark, QuranSurfaceLight)

val QuranSurfaceHigh: Color
    @Composable get() = quranColor(QuranSurfaceHighDark, QuranSurfaceHighLight)

val QuranPrimary: Color
    @Composable get() = quranColor(QuranPrimaryDark, QuranPrimaryLight)

val QuranOnPrimary: Color
    @Composable get() = quranColor(QuranOnPrimaryDark, QuranOnPrimaryLight)

val QuranPrimaryContainer: Color
    @Composable get() = quranColor(QuranPrimaryContainerDark, QuranPrimaryContainerLight)

val QuranOnPrimaryContainer: Color
    @Composable get() = quranColor(QuranOnPrimaryContainerDark, QuranOnPrimaryContainerLight)

val QuranArabicText: Color
    @Composable get() = quranColor(QuranArabicTextDark, QuranArabicTextLight)

val QuranTranslationText: Color
    @Composable get() = quranColor(QuranTranslationTextDark, QuranTranslationTextLight)

val QuranMutedText: Color
    @Composable get() = quranColor(QuranMutedTextDark, QuranMutedTextLight)

val QuranOutline: Color
    @Composable get() = quranColor(QuranOutlineDark, QuranOutlineLight)

val QuranError: Color
    @Composable get() = quranColor(QuranErrorDark, QuranErrorLight)

val QuranContinueCardGradientStart: Color
    @Composable get() = quranColor(QuranContinueCardGradientStartDark, QuranContinueCardGradientStartLight)

val QuranEntryProgressTrackColor: Color
    @Composable get() = quranColor(QuranEntryProgressTrackColorDark, QuranEntryProgressTrackColorLight)

@Composable
private fun quranColor(
    dark: Color,
    light: Color,
): Color = if (LocalQuranThemeMode.current == QuranThemeMode.LIGHT) light else dark
