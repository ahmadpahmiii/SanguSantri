package com.sangusantri.app.feature.quran

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sangusantri.app.core.designsystem.theme.LocalQuranThemeMode
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.QuranThemeMode

/**
 * Every Quran screen, dialog, and sheet renders in [LocalQuranThemeMode] regardless of the
 * surrounding app theme (QUR-FR-001, amended by the 2026-08-10 light-mode addition — ADR 0016).
 * Wraps [content] in a [SanguSantriTheme] matching that mode and sets system-bar icon appearance
 * to match (light icons on the dark backdrop, dark icons on the light one), restoring the previous
 * icon appearance when this leaves composition (the outer theme remains untouched).
 */
@Composable
fun QuranThemeBoundary(content: @Composable () -> Unit) {
    val mode = LocalQuranThemeMode.current
    val isDark = mode == QuranThemeMode.DARK
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view, isDark) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            QuranSystemBarBoundary.enter(controller, isDark)
            onDispose { QuranSystemBarBoundary.exit(controller) }
        }
    }
    SanguSantriTheme(darkTheme = isDark, content = content)
}

/** Navigation can compose the incoming Quran destination before disposing the outgoing one. A
 * shared count prevents the outgoing screen from restoring a stale icon appearance during that
 * overlap. */
private object QuranSystemBarBoundary {
    private var activeCount = 0
    private var previousLightStatusBars: Boolean? = null
    private var previousLightNavigationBars: Boolean? = null

    fun enter(
        controller: androidx.core.view.WindowInsetsControllerCompat?,
        isDark: Boolean,
    ) {
        if (activeCount == 0) {
            previousLightStatusBars = controller?.isAppearanceLightStatusBars
            previousLightNavigationBars = controller?.isAppearanceLightNavigationBars
        }
        activeCount += 1
        controller?.isAppearanceLightStatusBars = !isDark
        controller?.isAppearanceLightNavigationBars = !isDark
    }

    fun exit(controller: androidx.core.view.WindowInsetsControllerCompat?) {
        activeCount = (activeCount - 1).coerceAtLeast(0)
        if (activeCount > 0) return
        previousLightStatusBars?.let { controller?.isAppearanceLightStatusBars = it }
        previousLightNavigationBars?.let { controller?.isAppearanceLightNavigationBars = it }
        previousLightStatusBars = null
        previousLightNavigationBars = null
    }
}
