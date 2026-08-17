package com.sangusantri.app.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sangusantri.app.domain.model.AppThemeMode

/**
 * Live-resolved Quran colour roles, following the app-wide [LocalAppThemeMode]. Each property keeps
 * the exact name every Quran screen already imports from `Color.kt` before the light/dark split —
 * only the source file changed, so no screen's imports needed touching to gain light-mode support.
 *
 * Since the Beranda/Quran revamp these resolve to the same app-wide palette `Theme.kt` feeds into
 * `MaterialTheme.colorScheme`; they survive as named reader roles (Arabic text, translation text,
 * surface-high) that Material 3's scheme has no equivalent for.
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
): Color = if (LocalAppThemeMode.current == AppThemeMode.LIGHT) light else dark
