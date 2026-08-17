package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.sangusantri.app.domain.model.AppThemeMode

// Both schemes are built from the one app-wide palette in `Color.kt` (Beranda/Quran revamp) — the
// same roles the Quran reading surfaces resolve to, so the whole app is one colour family. Nothing
// outside this file reads a `Santri*` colour directly; every screen renders from
// `MaterialTheme.colorScheme`, which is why the revamp's palette change reaches every existing
// screen without touching one of them.
//
// `surfaceVariant` carries the design's "tint" role (icon tiles, chips, selected segments) rather
// than a neutral variant surface, and `onSurfaceVariant` carries muted secondary text — that is
// how the design frames use them.
private val DarkColorScheme =
    darkColorScheme(
        primary = SantriPrimaryDark,
        onPrimary = SantriOnPrimaryDark,
        primaryContainer = SantriTintDark,
        onPrimaryContainer = SantriOnTintDark,
        secondary = SantriOnTintDark,
        onSecondary = SantriBackgroundDark,
        background = SantriBackgroundDark,
        onBackground = SantriTextDark,
        surface = SantriSurfaceDark,
        onSurface = SantriTextDark,
        surfaceVariant = SantriTintDark,
        // The design has exactly two neutrals — background and surface — and no
        // elevation, so every container role collapses onto surface. Left unset these
        // fall back to Material 3's baseline lavender, which is what the bottom
        // navigation bar and sheets were rendering before the revamp.
        surfaceContainerLowest = SantriSurfaceDark,
        surfaceContainerLow = SantriSurfaceDark,
        surfaceContainer = SantriSurfaceDark,
        surfaceContainerHigh = SantriSurfaceDark,
        surfaceContainerHighest = SantriSurfaceDark,
        onSurfaceVariant = SantriMutedTextDark,
        outline = SantriOutlineDark,
        outlineVariant = SantriOutlineDark,
        error = SantriError80,
        onError = SantriError10,
        errorContainer = SantriError40,
        onErrorContainer = SantriError90,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SantriPrimaryLight,
        onPrimary = SantriOnPrimaryLight,
        primaryContainer = SantriTintLight,
        onPrimaryContainer = SantriOnTintLight,
        secondary = SantriOnTintLight,
        onSecondary = SantriOnPrimaryLight,
        background = SantriBackgroundLight,
        onBackground = SantriTextLight,
        surface = SantriSurfaceLight,
        onSurface = SantriTextLight,
        surfaceVariant = SantriTintLight,
        // The design has exactly two neutrals — background and surface — and no
        // elevation, so every container role collapses onto surface. Left unset these
        // fall back to Material 3's baseline lavender, which is what the bottom
        // navigation bar and sheets were rendering before the revamp.
        surfaceContainerLowest = SantriSurfaceLight,
        surfaceContainerLow = SantriSurfaceLight,
        surfaceContainer = SantriSurfaceLight,
        surfaceContainerHigh = SantriSurfaceLight,
        surfaceContainerHighest = SantriSurfaceLight,
        onSurfaceVariant = SantriMutedTextLight,
        outline = SantriOutlineLight,
        outlineVariant = SantriOutlineLight,
        error = SantriError40,
        onError = SantriOnPrimaryLight,
        errorContainer = SantriError90,
        onErrorContainer = SantriError10,
    )

/**
 * The app's resolved light/dark mode, provided once by [com.sangusantri.app.MainActivity] from the
 * persisted [AppThemeMode] (or the system setting while the user has never chosen one) and read by
 * every screen that needs the concrete mode rather than a colour — the theme toggle's icon, and the
 * Quran roles in `QuranColorScheme.kt`. Always a concrete [AppThemeMode.LIGHT]/[AppThemeMode.DARK];
 * "follow the system" is resolved before it reaches here. The [AppThemeMode.DARK] fallback applies
 * only to composables rendered outside that provider, e.g. `@Preview`s.
 */
val LocalAppThemeMode = compositionLocalOf { AppThemeMode.DARK }

/**
 * SanguSantri's Material 3 theme. Dynamic color is intentionally not offered:
 * the green Islamic identity must stay consistent across devices (PRD 13.8).
 */
@Composable
fun SanguSantriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SanguSantriTypography,
        shapes = SanguSantriShapes,
        content = content,
    )
}
