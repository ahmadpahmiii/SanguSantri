package com.sangusantri.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Baseline Material 3 typography. The approved Arabic typeface is a blocking
 * production input (PRD 13.9, 25.8) and is not yet available, so Arabic-specific
 * text styles are added when that font is supplied.
 */
val SanguSantriTypography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    )
