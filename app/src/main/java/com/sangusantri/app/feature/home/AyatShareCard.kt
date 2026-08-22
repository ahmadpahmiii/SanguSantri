package com.sangusantri.app.feature.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback
import kotlin.math.roundToInt

/**
 * Handoff turn 5 §3–4 — the 1:1 ayah card.
 *
 * One composable for both jobs on purpose: the sheet's preview and the PNG that actually gets sent
 * are the same composition, so what the reader approves is exactly what leaves the device. It is
 * also why the card is sized by its caller and squares itself, rather than pinning 296dp here.
 *
 * **Full text, never clipped.** The Beranda header clips to keep the page compact; this is the
 * payoff for "Selengkapnya", so nothing here caps a line count — a long ayah shrinks the whole
 * block instead (see [scaleDownToFit]).
 *
 * Background is the page background rather than the surface — once the card is a picture in someone
 * else's chat it has to read as its own object, not as a panel lifted off a screen.
 */
@Composable
fun AyatShareCard(
    ayat: AyatHariIni,
    arabicFont: QuranArabicFont,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(CardCornerRadius))
                .padding(CardPadding),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .scaleDownToFit(),
        ) {
            Text(
                text = ayat.arabicText.withQuranFontFallback(arabicFont),
                style =
                    TextStyle(
                        fontFamily = arabicFont.toFontFamily(),
                        fontSize = ArabicSize,
                        lineHeight = ArabicLineHeight,
                        textAlign = TextAlign.Center,
                        textDirection = TextDirection.Rtl,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = ayat.translation,
                fontSize = TranslationSize,
                lineHeight = TranslationLineHeight,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TranslationTopPadding),
            )
            Text(
                text = stringResource(R.string.beranda_ayat_share_card_reference, ayat.surahName, ayat.ayatNumber),
                fontSize = ReferenceSize,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = ReferenceTopPadding),
            )
            Text(
                text = stringResource(R.string.beranda_ayat_share_card_wordmark),
                fontSize = WordmarkSize,
                letterSpacing = 1.4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = WordmarkTopPadding),
            )
        }
    }
}

/**
 * Shrinks the content — as one block, so the type hierarchy is preserved — until it fits the square,
 * and never grows it.
 *
 * The design's sizes are drawn for an ayah of ordinary length, and the card has to hold all 6,236:
 * Al-Baqarah 282 is a page of text and no fixed size fits it in a 1:1 card. Scaling here rather than
 * auto-sizing each `Text` separately is deliberate — independent auto-sizing would let the
 * translation catch up with the Arabic on a long ayah and the two would stop reading as Qur'an plus
 * gloss.
 *
 * Measured in one pass, so there is no intermediate frame at the wrong size to flicker.
 */
private fun Modifier.scaleDownToFit(): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
        val available = constraints.maxHeight
        val scale =
            if (placeable.height > available && placeable.height > 0) {
                available.toFloat() / placeable.height
            } else {
                1f
            }
        layout(constraints.maxWidth, available) {
            placeable.placeWithLayer(
                x = 0,
                y = ((available - placeable.height * scale) / 2).roundToInt(),
            ) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
        }
    }

/** "QS. Ar-Ra'd : 28" — the citation form, outside a composition for the copied text. The card
 * above resolves the same resource through `stringResource`, so the picture and a pasted quote name
 * the source identically. */
fun Context.ayatReference(ayat: AyatHariIni): String =
    getString(R.string.beranda_ayat_share_card_reference, ayat.surahName, ayat.ayatNumber)

private val CardCornerRadius = 16.dp
private val CardPadding = 24.dp
private val ArabicSize = 20.sp
private val ArabicLineHeight = 2.25.em
private val TranslationSize = 12.sp
private val TranslationLineHeight = 1.68.em
private val TranslationTopPadding = 15.dp
private val ReferenceSize = 11.5.sp
private val ReferenceTopPadding = 13.dp
private val WordmarkSize = 9.5.sp
private val WordmarkTopPadding = 16.dp
