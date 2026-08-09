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
    if (view.isInEditMode || brightnessOverride == null) return
    DisposableEffect(view, brightnessOverride) {
        val window = (view.context as? Activity)?.window
        QuranWindowBrightnessBoundary.enter(window, brightnessOverride)
        onDispose { QuranWindowBrightnessBoundary.exit(window) }
    }
}

/** Keeps navigation overlap between two Quran destinations from restoring brightness too early. */
private object QuranWindowBrightnessBoundary {
    private var activeCount = 0
    private var previousBrightness: Float? = null

    fun enter(
        window: android.view.Window?,
        brightness: Float,
    ) {
        if (window == null) return
        if (activeCount == 0) previousBrightness = window.attributes.screenBrightness
        activeCount += 1
        setBrightness(window, brightness)
    }

    fun exit(window: android.view.Window?) {
        if (window == null) return
        activeCount = (activeCount - 1).coerceAtLeast(0)
        if (activeCount > 0) return
        previousBrightness?.let { setBrightness(window, it) }
        previousBrightness = null
    }

    private fun setBrightness(
        window: android.view.Window,
        brightness: Float,
    ) {
        val attributes = window.attributes
        attributes.screenBrightness = brightness
        window.attributes = attributes
    }
}
