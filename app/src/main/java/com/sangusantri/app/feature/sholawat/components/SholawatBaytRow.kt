package com.sangusantri.app.feature.sholawat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.arabicTextStyle
import com.sangusantri.app.core.designsystem.theme.translationTextStyle
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback
import com.sangusantri.app.feature.sholawat.SholawatReaderRow

/**
 * One bait of a qasidah — two hemistichs side by side, sadr on the right and ajuz on the left, the
 * way a qasidah is printed. Used only for content the CMS marked
 * [com.sangusantri.app.domain.model.ContentLayout.BAYT]; everything else keeps
 * [SholawatVerseBlock]'s one-verse-per-row scroll.
 *
 * The right-first order is [LayoutDirection.Rtl] doing its job, not position arithmetic: an RTL
 * `Row` places its first child at the right edge, so [bayt]`[0]` lands where the reader's eye
 * starts. Nothing here computes a column position.
 *
 * A hemistich is a fixed amount of text in a fixed half-width, so it is auto-sized rather than
 * given one size that is either too small for a short hemistich or too wide for a long one. The
 * ceiling is the user's own `arabicFontSizeSp` from [settings] and the floor is
 * [HEMISTICH_MIN_SIZE_RATIO] of it, so the reader's size stepper still drives this text — it just
 * cannot overflow a half-width cell. [arabicTextStyle] and [withQuranFontFallback] still supply the
 * typography and the glyph fallback; this is a width constraint, not a second Arabic type scale.
 *
 * [hemistichs] carries one or two. One means the item has an odd step count, which is a content
 * mistake the CMS editor refuses to save — but content already published this way still has to
 * read, so the orphan hemistich renders alone in the right column rather than being dropped.
 *
 * [translations] carries either one per hemistich (paired steps, each with its own translation) or
 * a single entry for the whole bait (one step split on its `۞`, where the translation is one
 * sentence). See [com.sangusantri.app.feature.sholawat.toSholawatReaderRows].
 */
@Composable
fun SholawatBaytRow(
    bait: SholawatReaderRow.Bait,
    showTranslation: Boolean,
    settings: ReaderSettings,
    arabicFont: QuranArabicFont,
    modifier: Modifier = Modifier,
) {
    val sadr = bait.hemistichs.firstOrNull() ?: return
    val ajuz = bait.hemistichs.getOrNull(1)

    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Hemistich(
                    text = sadr,
                    settings = settings,
                    arabicFont = arabicFont,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = BAYT_ORNAMENT,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = SanguSantriSpacing.small),
                )
                if (ajuz == null) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Hemistich(
                        text = ajuz,
                        settings = settings,
                        arabicFont = arabicFont,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (showTranslation && bait.translations.any { it.isNotBlank() }) {
            Spacer(modifier = Modifier.height(SanguSantriSpacing.small))
            BaitTranslations(translations = bait.translations, settings = settings)
        }
    }
}

/**
 * One translation means the whole bait is translated as a single sentence (the CMS stored the bait
 * as one step), so it runs full width rather than being cut in half to sit under two columns it
 * does not divide along. Two means each hemistich has its own.
 */
@Composable
private fun BaitTranslations(
    translations: List<String>,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
) {
    if (translations.size < 2) {
        HemistichTranslation(
            text = translations.first(),
            settings = settings,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    // LTR, so each Indonesian line starts at its own cell's left edge — but the cells are ordered
    // to sit under the hemistich they translate, which in an RTL bait puts the *second* hemistich
    // on the left. Column position alone carries the pairing; there is deliberately no ordinal or
    // other prefix on the text.
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        HemistichTranslation(text = translations[1], settings = settings, modifier = Modifier.weight(1f))
        HemistichTranslation(text = translations[0], settings = settings, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Hemistich(
    text: String,
    settings: ReaderSettings,
    arabicFont: QuranArabicFont,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text.withQuranFontFallback(arabicFont),
        style =
            arabicTextStyle(
                // Overridden by autoSize below; passed because arabicTextStyle needs a size.
                fontSizeSp = settings.arabicFontSizeSp,
                lineSpacingMultiplier = settings.arabicLineSpacingMultiplier,
                fontFamily = arabicFont.toFontFamily(),
            ).copy(color = MaterialTheme.colorScheme.onSurface),
        maxLines = HEMISTICH_MAX_LINES,
        autoSize =
            TextAutoSize.StepBased(
                minFontSize = hemistichMinFontSizeSp(settings.arabicFontSizeSp).sp,
                maxFontSize = settings.arabicFontSizeSp.sp,
            ),
        modifier = modifier,
    )
}

@Composable
private fun HemistichTranslation(
    text: String,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style =
            translationTextStyle(
                fontSizeSp = settings.translationFontSizeSp,
                lineSpacingMultiplier = settings.translationLineSpacingMultiplier,
            ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Six-petalled rosette, the conventional printed separator between the two hemistichs of a bait. */
private const val BAYT_ORNAMENT = "✻"

/**
 * How small a long hemistich may go before it is allowed to wrap to its second line. Scales with
 * the user's chosen size so the stepper still moves this text, but never below
 * [ReaderSettings.MIN_ARABIC_FONT_SIZE_SP] — that floor is the smallest size the reader offers
 * anywhere, and auto-size quietly undercutting it would be the two-column layout deciding the user
 * reads smaller than they asked to.
 *
 * A floor equal to the ceiling is fine: `TextAutoSize.StepBased` clamps that case rather than
 * throwing (its KDoc says otherwise; the implementation does not), and it simply means a fixed size.
 */
private fun hemistichMinFontSizeSp(arabicFontSizeSp: Int): Int =
    (arabicFontSizeSp * HEMISTICH_MIN_SIZE_RATIO).toInt().coerceAtLeast(ReaderSettings.MIN_ARABIC_FONT_SIZE_SP)

// ponytail: fixed ratio, tuned by eye against Salamun Salam's longest and shortest hemistich on a
// 360dp phone. Lower it if a qasidah with much longer hemistichs is published; the user's stepper
// sets the ceiling, so only the floor is tuned here.
private const val HEMISTICH_MIN_SIZE_RATIO = 0.62f

/** Two lines, because a hemistich that needs three in half a screen is not a hemistich. */
private const val HEMISTICH_MAX_LINES = 2
