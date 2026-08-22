package com.sangusantri.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * Handoff turn 5 §1 — the daily ayah, directly under the greeting and above the prayer block, the
 * two together forming one compact page header. Containerless by design: no card, no border, no
 * elevation, just type on the page background.
 *
 * **Clipped by line count, never by a fixed height.** One line of Arabic and two of translation is
 * what makes Al-Baqarah 286 and Ar-Ra'd 28 occupy identical space, on a 320dp device as much as on
 * a tall one. "Selengkapnya" appears only when a text genuinely overflowed — read back from
 * `onTextLayout`, not guessed from a character count, because whether 40 characters of Arabic fit
 * depends on the face, the user's font scale and the screen.
 *
 * The whole block is one tap target; the full, unclipped text lives in the sheet.
 */
@Composable
fun BerandaAyatHariIni(
    ayat: AyatHariIni,
    arabicFont: QuranArabicFont,
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var arabicOverflowed by remember(ayat) { mutableStateOf(false) }
    var translationOverflowed by remember(ayat) { mutableStateOf(false) }

    Column(modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onOpenSheet)) {
        LabelRow(reference = ayat.reference)
        Text(
            text = ayat.arabicText.withQuranFontFallback(arabicFont),
            style =
                TextStyle(
                    fontFamily = arabicFont.toFontFamily(),
                    fontSize = ArabicSize,
                    lineHeight = ArabicLineHeight,
                    textAlign = TextAlign.Right,
                    textDirection = TextDirection.Rtl,
                ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { arabicOverflowed = it.hasVisualOverflow },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ArabicTopPadding),
        )
        Text(
            text = ayat.translation,
            fontSize = TranslationSize,
            lineHeight = TranslationLineHeight,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { translationOverflowed = it.hasVisualOverflow },
            modifier = Modifier.padding(top = TranslationTopPadding),
        )
        if (arabicOverflowed || translationOverflowed) MoreLink()
    }
}

@Composable
private fun LabelRow(reference: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.beranda_ayat_label),
            fontSize = LabelSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = reference,
            fontSize = ReferenceSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = LabelRowGap),
        )
    }
}

/** Rendered only when a text actually overflowed — the header carries no permanent affordance,
 * because on a short ayah there would be nothing more to show. */
@Composable
private fun MoreLink() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MoreLinkGap),
        modifier = Modifier.padding(top = TranslationTopPadding),
    ) {
        Text(
            text = stringResource(R.string.beranda_ayat_more),
            fontSize = MoreLinkSize,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(MoreLinkIconSize),
        )
    }
}

private val LabelSize = 10.5.sp
private val ReferenceSize = 11.sp
private val ArabicSize = 19.sp
private val ArabicLineHeight = 2.05.em
private val ArabicTopPadding = 9.dp
private val TranslationSize = 12.5.sp
private val TranslationLineHeight = 1.6.em
private val TranslationTopPadding = 7.dp
private val MoreLinkSize = 12.sp
private val MoreLinkIconSize = 15.dp
private val MoreLinkGap = 3.dp
private val LabelRowGap = 10.dp
