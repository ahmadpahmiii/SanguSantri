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
import com.sangusantri.app.domain.model.ReaderSettings

/**
 * One verse in the Sholawat reader (0.0.8's own reader, not [com.sangusantri.app.feature.reader]'s
 * `ReaderStepFields` — see ADR-less rationale in `docs/product/SHOLAWAT_PRD.md`). Reuses
 * [arabicTextStyle]/[translationTextStyle] (the same RTL-aware, already-approved text styles Full
 * Reader uses) rather than re-deriving Arabic typography from scratch. [largeArabicMode] is the
 * screen-wide "Arabic-only, bigger font" toggle (0.0.8: still scrolls, not a shrink-to-fit
 * algorithm) — reusing [ReaderSettings]'s existing documented font-size range rather than
 * inventing a new one.
 */
@Composable
fun SholawatVerseBlock(
    step: ContentStep,
    showTranslation: Boolean,
    largeArabicMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            SelectionContainer {
                Text(
                    text = step.arabicText,
                    style =
                        arabicTextStyle(
                            fontSizeSp =
                                if (largeArabicMode) {
                                    ReaderSettings.MAX_ARABIC_FONT_SIZE_SP
                                } else {
                                    ReaderSettings.DEFAULT_ARABIC_FONT_SIZE_SP
                                },
                            lineSpacingMultiplier = ReaderSettings.DEFAULT_ARABIC_LINE_SPACING,
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
                            fontSizeSp = ReaderSettings.DEFAULT_TRANSLATION_FONT_SIZE_SP,
                            lineSpacingMultiplier = ReaderSettings.DEFAULT_TRANSLATION_LINE_SPACING,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
