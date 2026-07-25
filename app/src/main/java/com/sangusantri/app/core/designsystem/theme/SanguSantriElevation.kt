package com.sangusantri.app.core.designsystem.theme

import androidx.compose.ui.unit.dp
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation.outlineWidth

/**
 * Elevation policy (DESIGN_SYSTEM.md): prefer tonal surfaces or a hairline border over shadow
 * stacking — Material's default elevation shadows read as generic at default settings.
 * Bounded, tappable units (e.g. amaliyah cards) use [outlineWidth] on a flat surface instead of
 * [Card]'s default shadow elevation.
 */
object SanguSantriElevation {
    val flat = 0.dp
    val outlineWidth = 1.dp
}
