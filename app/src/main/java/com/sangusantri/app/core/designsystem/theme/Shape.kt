package com.sangusantri.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Three deliberate corner radii (DESIGN_SYSTEM.md) — not one radius invented per component.
 * `small` for compact controls, `medium` for cards, `large` for sheets/dialogs.
 */
val SanguSantriShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
    )
