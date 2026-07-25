package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
        error = SantriError80,
        onError = SantriError10,
        errorContainer = SantriError40,
        onErrorContainer = SantriError90,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SantriGreen40,
        onPrimary = SantriNeutral99,
        primaryContainer = SantriGreen90,
        onPrimaryContainer = SantriGreen10,
        secondary = SantriGreen30,
        onSecondary = SantriNeutral99,
        background = SantriNeutral99,
        onBackground = SantriNeutral10,
        surface = SantriNeutral95,
        onSurface = SantriNeutral10,
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
