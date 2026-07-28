package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// `surfaceVariant`/`onSurfaceVariant`/`outline`/`outlineVariant` are explicitly set below (Figma
// product-alignment pass) — they were previously left unset here, so every existing usage
// (AmaliyahCard/ReaderStepItem borders, Reader/Serambi secondary text) was silently rendering
// Material 3's unbranded default color instead of a SanguSantri token. No revised dark-mode Figma
// frame was exported, so the dark values below reuse existing green-ramp tokens as a reasonable
// approximation rather than inventing new, unverified hex values (`docs/design/FIGMA_HANDOFF.md`).
private val DarkColorScheme =
    darkColorScheme(
        primary = SantriGreen80,
        onPrimary = SantriGreen20,
        primaryContainer = SantriGreen30,
        onPrimaryContainer = SantriGreen90,
        secondary = SantriGreen90,
        onSecondary = SantriGreen10,
        background = SantriNeutral10,
        onBackground = SantriNeutral90,
        surface = SantriNeutral10,
        onSurface = SantriNeutral90,
        surfaceVariant = SantriGreen20,
        onSurfaceVariant = SantriGreen90,
        outline = SantriGreen30,
        outlineVariant = SantriGreen30,
        error = SantriError80,
        onError = SantriError10,
        errorContainer = SantriError40,
        onErrorContainer = SantriError90,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SantriGreen40,
        onPrimary = SantriNeutral99,
        primaryContainer = SantriGreen95,
        onPrimaryContainer = SantriGreen20,
        secondary = SantriGreen30,
        onSecondary = SantriNeutral99,
        background = SantriNeutral99,
        onBackground = SantriNeutral10,
        surface = SantriSurface,
        onSurface = SantriNeutral10,
        surfaceVariant = SantriNeutral95,
        onSurfaceVariant = SantriNeutral40,
        outline = SantriOutline,
        outlineVariant = SantriOutline,
        error = SantriError40,
        onError = SantriNeutral99,
        errorContainer = SantriError90,
        onErrorContainer = SantriError10,
    )

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
