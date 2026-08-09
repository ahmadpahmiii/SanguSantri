package com.sangusantri.app.feature.quran.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
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
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

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
        Row(modifier = Modifier.fillMaxWidth()) {
            QuranAvailableFontCard(
                name = stringResource(R.string.quran_font_candidate_lpmq),
                font = QuranArabicFont.LPMQ_ISEP_MISBAH,
                selected = selectedFont == QuranArabicFont.LPMQ_ISEP_MISBAH,
                sampleText = sampleText,
                onSelected = onFontSelected,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(SanguSantriSpacing.small))
            QuranAvailableFontCard(
                name = stringResource(R.string.quran_font_candidate_amiri),
                font = QuranArabicFont.AMIRI_QURAN,
                selected = selectedFont == QuranArabicFont.AMIRI_QURAN,
                sampleText = sampleText,
                onSelected = onFontSelected,
                modifier = Modifier.weight(1f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            QuranUnavailableFontCard(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(SanguSantriSpacing.small))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun QuranAvailableFontCard(
    name: String,
    font: QuranArabicFont,
    selected: Boolean,
    sampleText: String?,
    onSelected: (QuranArabicFont) -> Unit,
    modifier: Modifier = Modifier,
) {
    QuranFontCardSurface(
        selected = selected,
        modifier =
            modifier.selectable(
                selected = selected,
                onClick = { onSelected(font) },
                role = Role.RadioButton,
            ),
    ) {
        Row {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = QuranPrimary, unselectedColor = QuranMutedText),
            )
            Text(
                text = name,
                color = if (selected) QuranOnPrimaryContainer else QuranArabicText,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = SanguSantriSpacing.small),
            )
        }
        if (!sampleText.isNullOrBlank()) {
            Text(
                text = sampleText.withQuranFontFallback(font),
                color = QuranArabicText,
                fontFamily = font.toFontFamily(),
                fontSize = FONT_SAMPLE_SIZE_SP.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun QuranUnavailableFontCard(modifier: Modifier = Modifier) {
    QuranFontCardSurface(selected = false, modifier = modifier.alpha(DISABLED_FONT_ALPHA)) {
        Row {
            RadioButton(selected = false, onClick = null, enabled = false)
            Text(
                text = stringResource(R.string.quran_font_candidate_king_fahd),
                color = QuranMutedText,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = SanguSantriSpacing.small),
            )
        }
        Text(
            text = stringResource(R.string.quran_font_unavailable),
            color = QuranMutedText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = SanguSantriSpacing.small),
        )
    }
}

@Composable
private fun QuranFontCardSurface(
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = if (selected) QuranPrimaryContainer else QuranSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, if (selected) QuranPrimary else QuranOutline),
        modifier =
            modifier.heightIn(
                min = SanguSantriDimensions.minimumTouchTarget + FONT_CARD_EXTRA_HEIGHT,
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier = Modifier.padding(SanguSantriSpacing.small),
            content = content,
        )
    }
}

private const val FONT_SAMPLE_SIZE_SP = 22
private const val DISABLED_FONT_ALPHA = 0.48f
private val FONT_CARD_EXTRA_HEIGHT = 48.dp
