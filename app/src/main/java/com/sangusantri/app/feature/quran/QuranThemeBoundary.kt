package com.sangusantri.app.feature.quran

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * Every Quran screen, dialog, and sheet is dark-only regardless of the surrounding app theme
 * (QUR-FR-001). Wraps [content] in a forced-dark [SanguSantriTheme] and uses light system-bar icons
 * against the navigation host's Quran-dark inset backdrop, restoring the previous icon appearance
 * when this leaves composition (the outer theme remains untouched).
 */
@Composable
fun QuranThemeBoundary(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            QuranSystemBarBoundary.enter(controller)
            onDispose { QuranSystemBarBoundary.exit(controller) }
        }
    }
    SanguSantriTheme(darkTheme = true, content = content)
}

/** Navigation can compose the incoming Quran destination before disposing the outgoing one. A
 * shared count prevents the outgoing screen from restoring a stale icon appearance during that
 * overlap. */
private object QuranSystemBarBoundary {
    private var activeCount = 0
    private var previousLightStatusBars: Boolean? = null
    private var previousLightNavigationBars: Boolean? = null

    fun enter(controller: androidx.core.view.WindowInsetsControllerCompat?) {
        if (activeCount == 0) {
            previousLightStatusBars = controller?.isAppearanceLightStatusBars
            previousLightNavigationBars = controller?.isAppearanceLightNavigationBars
        }
        activeCount += 1
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
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
