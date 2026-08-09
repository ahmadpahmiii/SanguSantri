package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Three deliberate corner radii (DESIGN_SYSTEM.md) — not one radius invented per component.
 * `small` for compact controls, `medium` for cards, `large` for sheets/dialogs. `extraLarge` is a
 * full stadium/pill (design product-alignment pass — every pill-shaped element in the revised
 * exports — repeat-shortcut actions, the saved-position status, stepper value controls, the
 * guided/tasbih counter, tasbih presets — uses a corner radius equal to half its own height, which
 * `RoundedCornerShape(percent = 50)` reproduces at any size rather than a fixed dp value).
 */
val SanguSantriShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(percent = 50),
    )
