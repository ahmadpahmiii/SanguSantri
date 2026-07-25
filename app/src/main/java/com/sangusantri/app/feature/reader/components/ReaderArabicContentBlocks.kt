package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.sangusantri.app.R
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

/** Visually distinct but subordinate to the Arabic text it precedes (Milestone 3 §5). */
@Composable
internal fun ReaderQuranReference(
    surah: Int,
    ayahStart: Int,
    ayahEnd: Int?,
) {
    val text =
        if (ayahEnd != null && ayahEnd != ayahStart) {
            stringResource(R.string.reader_quran_reference_range, surah, ayahStart, ayahEnd)
        } else {
            stringResource(R.string.reader_quran_reference_single, surah, ayahStart)
        }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** Informational only for Milestone 3 — must not resemble an interactive counter (no border/badge). */
@Composable
internal fun ReaderRepetitionIndicator(target: Int) {
    Text(
        text = stringResource(R.string.reader_repetition_target, target),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
}
