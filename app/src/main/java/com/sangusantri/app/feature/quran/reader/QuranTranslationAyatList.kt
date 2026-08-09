package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranTranslationText
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * Arab + translation rendering: one stable lazy item per ordered ayat.
 *
 * [lazyListState] defaults to a locally remembered state for the existing debug-only preview
 * caller, but the real reader (`feature/quran/reader/QuranReaderScreen.kt`) supplies its own so it
 * can derive the currently visible ayat for last-read/reading-session tracking (QUR-FR-011/017).
 * [headerContent], when supplied, renders as the list's first item (the surah-start header/basmalah,
 * QUR-FR-010) so it scrolls away naturally instead of staying pinned.
 */
@Suppress("LongParameterList")
@Composable
fun QuranTranslationAyatList(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    arabicSizeSp: Int = DEFAULT_ARABIC_SIZE_SP,
    arabicLineHeightSp: Int = DEFAULT_ARABIC_LINE_HEIGHT_SP,
    translationSizeSp: Int = DEFAULT_TRANSLATION_SIZE_SP,
    arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    headerContent: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize(),
        ) {
            if (headerContent != null) {
                item(key = "surah-header") { headerContent() }
            }
            items(items = ayats, key = { it.remoteId }) { ayat ->
                QuranTranslationAyatItem(
                    ayat = ayat,
                    selected = ayat.remoteId == selectedAyatId,
                    onLongPress = { onAyatLongPress(ayat) },
                    arabicSizeSp = arabicSizeSp,
                    arabicLineHeightSp = arabicLineHeightSp,
                    translationSizeSp = translationSizeSp,
                    arabicFont = arabicFont,
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun QuranTranslationAyatItem(
    ayat: QuranReaderAyatUiModel,
    selected: Boolean,
    onLongPress: () -> Unit,
    arabicSizeSp: Int,
    arabicLineHeightSp: Int,
    translationSizeSp: Int,
    arabicFont: QuranArabicFont,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val semanticsLabel = stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber)
    val selectionColor = QuranPrimaryContainer
    Column(
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier =
            Modifier
                .fillMaxWidth()
                .drawBehind { if (selected) drawRect(selectionColor) }
                .semantics {
                    onLongClick(label = semanticsLabel) {
                        onLongPress()
                        true
                    }
                }.pointerInput(ayat.remoteId) {
                    detectTapGestures(
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    )
                }.padding(
                    horizontal = SanguSantriDimensions.readerHorizontalPadding,
                    vertical = SanguSantriSpacing.large,
                ),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            // TextAlign.End resolves to the *left* edge once layout direction is Rtl (End is
            // logical, relative to direction) — Right is the physical alignment this needs.
            Text(
                text = ayat.arabicText.withQuranFontFallback(arabicFont),
                style =
                    TextStyle(
                        fontFamily = arabicFont.toFontFamily(),
                        fontSize = arabicSizeSp.sp,
                        lineHeight = arabicLineHeightSp.sp,
                        textAlign = TextAlign.Right,
                    ),
                color = QuranArabicText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = ayat.translation,
            color = QuranTranslationText,
            fontSize = translationSizeSp.sp,
            lineHeight = (translationSizeSp * TRANSLATION_LINE_HEIGHT_MULTIPLIER).sp,
            modifier = Modifier.fillMaxWidth(),
        )
        QuranSourceAnnotations(ayat)
        Text(
            text = stringResource(R.string.quran_reader_ayat_context, ayat.ayatNumber, ayat.juz, ayat.page),
            color = QuranPrimary,
            style = MaterialTheme.typography.bodySmall,
        )
        HorizontalDivider(color = QuranOutline)
    }
}

@Composable
private fun QuranSourceAnnotations(ayat: QuranReaderAyatUiModel) {
    val footnote = ayat.footnoteText.ifBlank { ayat.footnoteNumber }
    listOf(ayat.note, footnote)
        .filter(String::isNotBlank)
        .forEach { sourceText ->
            Text(
                text = sourceText,
                color = QuranMutedText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
}

private const val DEFAULT_ARABIC_SIZE_SP = QuranReaderSettings.DEFAULT_ARABIC_SIZE_SP
private const val DEFAULT_ARABIC_LINE_HEIGHT_SP = 48
private const val DEFAULT_TRANSLATION_SIZE_SP = QuranReaderSettings.DEFAULT_TRANSLATION_SIZE_SP
private const val TRANSLATION_LINE_HEIGHT_MULTIPLIER = 1.55f
