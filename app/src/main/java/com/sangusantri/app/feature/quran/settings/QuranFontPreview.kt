package com.sangusantri.app.feature.quran.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * One full-width row per packaged face, each rendering the same real Kemenag ayat, so the choice is
 * made by reading the actual sample rather than by recognising a font name. [sampleText] is the
 * locally stored preview ayat and is `null` until local preparation completes.
 */
@Composable
internal fun QuranFontSelector(
    selectedFont: QuranArabicFont,
    sampleText: String?,
    onFontSelected: (QuranArabicFont) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        modifier = Modifier.selectableGroup(),
    ) {
        Text(
            text = stringResource(R.string.quran_settings_font_section_title),
            style = MaterialTheme.typography.titleMedium,
            color = QuranArabicText,
        )
        QuranArabicFont.entries.forEach { font ->
            QuranFontOptionRow(
                font = font,
                selected = font == selectedFont,
                sampleText = sampleText,
                onSelected = onFontSelected,
            )
        }
    }
}

@Composable
private fun QuranFontOptionRow(
    font: QuranArabicFont,
    selected: Boolean,
    sampleText: String?,
    onSelected: (QuranArabicFont) -> Unit,
) {
    Surface(
        color = if (selected) QuranPrimaryContainer else QuranSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, if (selected) QuranPrimary else QuranOutline),
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = { onSelected(font) },
                    role = Role.RadioButton,
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier = Modifier.padding(SanguSantriSpacing.small),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = QuranPrimary, unselectedColor = QuranMutedText),
                )
                Column {
                    Text(
                        text = stringResource(font.labelRes),
                        color = if (selected) QuranOnPrimaryContainer else QuranArabicText,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(font.descriptionRes),
                        color = QuranMutedText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (!sampleText.isNullOrBlank()) QuranFontSample(font = font, sampleText = sampleText)
        }
    }
}

/** The same stored ayat in one candidate face, right-aligned in its own RTL context. */
@Composable
private fun QuranFontSample(
    font: QuranArabicFont,
    sampleText: String,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = sampleText.withQuranFontFallback(font),
            color = QuranArabicText,
            style =
                TextStyle(
                    fontFamily = font.toFontFamily(),
                    fontSize = FONT_SAMPLE_SIZE_SP.sp,
                    lineHeight = FONT_SAMPLE_LINE_HEIGHT_SP.sp,
                    textAlign = TextAlign.Start,
                ),
            maxLines = FONT_SAMPLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.small),
        )
    }
}

private val QuranArabicFont.labelRes: Int
    @StringRes get() =
        when (this) {
            QuranArabicFont.LPMQ_ISEP_MISBAH -> R.string.quran_font_candidate_lpmq
            QuranArabicFont.KFGQPC_HAFS -> R.string.quran_font_candidate_hafs
            QuranArabicFont.AMIRI_QURAN -> R.string.quran_font_candidate_amiri
        }

private val QuranArabicFont.descriptionRes: Int
    @StringRes get() =
        when (this) {
            QuranArabicFont.LPMQ_ISEP_MISBAH -> R.string.quran_font_description_lpmq
            QuranArabicFont.KFGQPC_HAFS -> R.string.quran_font_description_hafs
            QuranArabicFont.AMIRI_QURAN -> R.string.quran_font_description_amiri
        }

private const val FONT_SAMPLE_SIZE_SP = 28
private const val FONT_SAMPLE_LINE_HEIGHT_SP = 56
private const val FONT_SAMPLE_MAX_LINES = 2
