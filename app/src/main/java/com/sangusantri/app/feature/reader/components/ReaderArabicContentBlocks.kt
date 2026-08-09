package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.arabicTextStyle
import com.sangusantri.app.core.designsystem.theme.translationTextStyle
import com.sangusantri.app.domain.model.ReaderSettings

@Composable
internal fun ReaderArabicBlock(
    text: String,
    settings: ReaderSettings,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        SelectionContainer {
            Text(
                text = text,
                style = arabicTextStyle(settings.arabicFontSizeSp, settings.arabicLineSpacingMultiplier),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun ReaderTranslationBlock(
    text: String,
    settings: ReaderSettings,
) {
    SelectionContainer {
        Text(
            text = text,
            style = translationTextStyle(settings.translationFontSizeSp, settings.translationLineSpacingMultiplier),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Full Reader's repetition shortcut (decision D, `docs/design/DESIGN_HANDOFF.md` node `14:2`):
 * "Dibaca N kali · Buka Panduan →" — tapping opens Guided Reader at this exact step immediately,
 * no confirmation. Styled as a tonal pill, visually secondary to the Arabic text above it, never a
 * dominant button (48dp minimum touch target still applies).
 */
@Composable
internal fun ReaderRepetitionShortcut(
    target: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = SanguSantriShapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Text(
            text = stringResource(R.string.reader_repetition_shortcut, target),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.medium),
        )
    }
}
