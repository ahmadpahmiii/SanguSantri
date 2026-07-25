package com.sangusantri.app.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Arabic and translation text styles for the Full Reader (DESIGN_SYSTEM.md: "a type scale that
 * gives Arabic text and Indonesian translation text distinct, deliberately different styles...
 * cannot be deferred past the first reader screen"). Font size and line spacing are
 * user-configurable (FR-008), so these are functions rather than fixed [Typography] entries.
 *
 * [FontFamily.Default] is used deliberately: no approved Arabic typeface exists yet (a Blocking
 * Production Input, PRD 13.9/25.8) and downloading one at runtime is prohibited. Android already
 * substitutes a script-appropriate system fallback font for Arabic glyphs under a Latin primary
 * family, so harakat render correctly without bundling anything — this is a documented interim
 * choice, not a final answer; replace with the approved typeface once supplied.
 */
fun arabicTextStyle(
    fontSizeSp: Int,
    lineSpacingMultiplier: Float,
    fontWeight: FontWeight = FontWeight.Normal,
): TextStyle =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeight,
        fontSize = fontSizeSp.sp,
        lineHeight = lineSpacingMultiplier.em,
        textAlign = TextAlign.Right,
    )

fun translationTextStyle(
    fontSizeSp: Int,
    lineSpacingMultiplier: Float,
): TextStyle =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = fontSizeSp.sp,
        lineHeight = lineSpacingMultiplier.em,
        letterSpacing = 0.15.sp,
    )
