package com.sangusantri.app.core.designsystem.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Standalone Tasbih nav icon (0.0.2) — a small arc of prayer beads around one larger "imam" bead.
 * Material Symbols has no dedicated tasbih glyph
 * (`docs/design/design-export/future-releases/00-overview-and-tokens.md`), so this is a
 * lightweight custom vector, drawn geometrically (never a raster image or Unicode glyph), with
 * the same filled/outlined states every other nav icon uses.
 */
@Composable
fun TasbihIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val ringRadius = size.minDimension * 0.34f
        val beadRadius = size.minDimension * 0.09f
        val imamBeadRadius = size.minDimension * 0.13f
        val strokeStyle = if (filled) null else Stroke(width = size.minDimension * 0.09f)
        val angleStep = (2 * Math.PI) / (BEAD_COUNT + 1)

        for (index in 1..BEAD_COUNT) {
            val angle = angleStep * index - Math.PI / 2
            val beadCenter =
                Offset(
                    x = center.x + (ringRadius * cos(angle)).toFloat(),
                    y = center.y + (ringRadius * sin(angle)).toFloat(),
                )
            if (strokeStyle == null) {
                drawCircle(color = tint, radius = beadRadius, center = beadCenter)
            } else {
                drawCircle(color = tint, radius = beadRadius, center = beadCenter, style = strokeStyle)
            }
        }

        val imamCenter = Offset(center.x, center.y - ringRadius)
        if (strokeStyle == null) {
            drawCircle(color = tint, radius = imamBeadRadius, center = imamCenter)
        } else {
            drawCircle(color = tint, radius = imamBeadRadius, center = imamCenter, style = strokeStyle)
        }
    }
}

private const val BEAD_COUNT = 6
