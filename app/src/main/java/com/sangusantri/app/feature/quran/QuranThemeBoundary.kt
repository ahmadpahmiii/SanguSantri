package com.sangusantri.app.feature.quran

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * Every Quran screen, dialog, and sheet is dark-only regardless of the surrounding app theme
 * (QUR-FR-001). Wraps [content] in a forced-dark [SanguSantriTheme] and switches system status/
 * navigation bar icon appearance to match, restoring the previous system-bar appearance when this
 * leaves composition (the outer [SanguSantriTheme] in `MainActivity` is untouched, so the app's own
 * theme choice is automatically what is seen again once the user navigates away from Quran).
 */
@Composable
fun QuranThemeBoundary(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(view) {
            val window = (view.context as? Activity)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, view) }
            val previousLightStatusBars = controller?.isAppearanceLightStatusBars
            val previousLightNavigationBars = controller?.isAppearanceLightNavigationBars
            controller?.isAppearanceLightStatusBars = false
            controller?.isAppearanceLightNavigationBars = false
            onDispose {
                if (controller != null && previousLightStatusBars != null) {
                    controller.isAppearanceLightStatusBars = previousLightStatusBars
                }
                if (controller != null && previousLightNavigationBars != null) {
                    controller.isAppearanceLightNavigationBars = previousLightNavigationBars
                }
            }
        }
    }
    SanguSantriTheme(darkTheme = true, content = content)
}
