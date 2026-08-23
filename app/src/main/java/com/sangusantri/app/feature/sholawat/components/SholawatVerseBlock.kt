package com.sangusantri.app.feature.sholawat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.arabicTextStyle
import com.sangusantri.app.core.designsystem.theme.translationTextStyle
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * One verse in the Sholawat reader (0.0.8's own reader, not [com.sangusantri.app.feature.reader]'s
 * `ReaderStepFields` — see ADR-less rationale in `docs/product/SHOLAWAT_PRD.md`). Reuses
 * [arabicTextStyle]/[translationTextStyle] (the same RTL-aware, already-approved text styles Full
 * Reader uses) rather than re-deriving Arabic typography from scratch.
 *
 * Sizes come from [settings], the same shared [ReaderSettings] store the Full and Guided Readers
 * write to, so the reader's own size stepper drives this text. It used to hard-code
 * `MAX_ARABIC_FONT_SIZE_SP` (40sp) whenever the translation was hidden, which is why Arabic-only
 * mode rendered far larger than the Quran reader and could not be turned down.
 *
 * [arabicFont] is the app-wide typeface chosen in the Quran reader's settings; words it has no
 * glyph for fall back to LPMQ exactly as they do in the other readers.
 */
@Composable
fun SholawatVerseBlock(
    step: ContentStep,
    showTranslation: Boolean,
    settings: ReaderSettings,
    arabicFont: QuranArabicFont,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            SelectionContainer {
                Text(
                    text = step.arabicText.withQuranFontFallback(arabicFont),
                    style =
                        arabicTextStyle(
                            fontSizeSp = settings.arabicFontSizeSp,
                            lineSpacingMultiplier = settings.arabicLineSpacingMultiplier,
                            fontFamily = arabicFont.toFontFamily(),
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (showTranslation) {
            Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
            SelectionContainer {
                Text(
                    text = step.translation,
                    style =
                        translationTextStyle(
                            fontSizeSp = settings.translationFontSizeSp,
                            lineSpacingMultiplier = settings.translationLineSpacingMultiplier,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
