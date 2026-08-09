package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Kalender Hijriah's (`0.0.7`) light/dark event-semantic roles — resolved the same way
 * [SanguSantriTheme] itself resolves light vs dark, so this palette always tracks the active theme. */
data class HijriCalendarPalette(
    val teal: Color,
    val tealSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val coral: Color,
    val coralSoft: Color,
)

@Composable
fun hijriCalendarPalette(): HijriCalendarPalette =
    if (isSystemInDarkTheme()) {
        HijriCalendarPalette(
            teal = HijriTealDark,
            tealSoft = HijriTealSoftDark,
            amber = HijriAmberDark,
            amberSoft = HijriAmberSoftDark,
            coral = HijriCoralDark,
            coralSoft = HijriCoralSoftDark,
        )
    } else {
        HijriCalendarPalette(
            teal = HijriTealLight,
            tealSoft = HijriTealSoftLight,
            amber = HijriAmberLight,
            amberSoft = HijriAmberSoftLight,
            coral = HijriCoralLight,
            coralSoft = HijriCoralSoftLight,
        )
    }
