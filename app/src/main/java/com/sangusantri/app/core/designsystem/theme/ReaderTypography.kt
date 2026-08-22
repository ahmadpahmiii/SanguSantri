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
 * [fontFamily] defaults to [FontFamily.Default] for call sites with no user-selectable typeface
 * (e.g. the Sholawat reader). The Full Reader and Guided Reader pass the shared
 * `ReaderSettings.arabicFont` typeface instead — see `QuranArabicFont.toFontFamily()`.
 */
fun arabicTextStyle(
    fontSizeSp: Int,
    lineSpacingMultiplier: Float,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.Default,
): TextStyle =
    TextStyle(
        fontFamily = fontFamily,
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
