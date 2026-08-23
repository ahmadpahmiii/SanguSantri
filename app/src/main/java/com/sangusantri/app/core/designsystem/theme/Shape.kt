package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * A small deliberate radius set (DESIGN_SYSTEM.md) — not one radius invented per component.
 * `small` for compact controls, `medium` for cards, `large` for menus/sheets, `extraLarge` for
 * dialogs: Material 3 derives every dialog's container shape from `extraLarge`, so it must stay a
 * real radius. Stadium/pill elements use [SanguSantriPillShape] explicitly instead.
 */
val SanguSantriShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

/**
 * Full stadium/pill — a corner radius equal to half the element's own height at any size (design
 * product-alignment pass: repeat-shortcut actions, saved-position status, stepper value controls,
 * progress tracks, badges, tasbih presets).
 */
val SanguSantriPillShape = RoundedCornerShape(percent = 50)
