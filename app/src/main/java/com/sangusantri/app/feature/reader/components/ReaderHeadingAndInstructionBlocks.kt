package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.arabicTextStyle
import com.sangusantri.app.domain.model.ReaderSettings

private const val INSTRUCTION_ARABIC_FONT_SIZE_SP = 20
private const val HEADING_ARABIC_FONT_SIZE_DELTA_SP = 4
private const val INSTRUCTION_ARABIC_LINE_SPACING = 1.6f

@Composable
internal fun ReaderHeadingText(
    text: String,
    isClosing: Boolean,
) {
    Text(
        text = text,
        style = if (isClosing) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
internal fun ReaderHeadingArabicText(
    text: String,
    settings: ReaderSettings,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        SelectionContainer {
            Text(
                text = text,
                style =
                    arabicTextStyle(
                        fontSizeSp =
                            (settings.arabicFontSizeSp - HEADING_ARABIC_FONT_SIZE_DELTA_SP)
                                .coerceAtLeast(ReaderSettings.MIN_ARABIC_FONT_SIZE_SP),
                        lineSpacingMultiplier = settings.arabicLineSpacingMultiplier,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Styled distinctly from devotional text (italic Indonesian, muted colour) per Milestone 3 §5. */
@Composable
internal fun ReaderInstructionText(
    instructionId: String?,
    instructionAr: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        instructionId?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        instructionAr?.takeIf { it.isNotBlank() }?.let {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = it,
                    style =
                        arabicTextStyle(
                            fontSizeSp = INSTRUCTION_ARABIC_FONT_SIZE_SP,
                            lineSpacingMultiplier = INSTRUCTION_ARABIC_LINE_SPACING,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
