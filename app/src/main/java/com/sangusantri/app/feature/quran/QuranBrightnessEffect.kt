package com.sangusantri.app.feature.quran

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Applies [brightnessOverride] (`0f..1f`) to only the current window, restoring the prior window
 * brightness value on dispose (QUR-FR-015) — `null` leaves the window's brightness exactly as it
 * was set by the system/app, never overriding it. Called from both the reader and the settings
 * screen so brightness applies wherever a Quran surface is visible.
 */
@Composable
fun QuranBrightnessEffect(brightnessOverride: Float?) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(brightnessOverride) {
        val window = (view.context as? Activity)?.window
        val previousBrightness = window?.attributes?.screenBrightness
        if (window != null && brightnessOverride != null) {
            val attributes = window.attributes
            attributes.screenBrightness = brightnessOverride
            window.attributes = attributes
        }
        onDispose {
            if (window != null && previousBrightness != null) {
                val attributes = window.attributes
                attributes.screenBrightness = previousBrightness
                window.attributes = attributes
            }
        }
    }
}
