package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
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
    onAyatSelected: (QuranReaderAyatUiModel) -> Unit = onAyatLongPress,
    headerContent: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize()
                    .padding(horizontal = SanguSantriDimensions.readerHorizontalPadding),
        ) {
            if (headerContent != null) {
                item(key = "surah-header") { headerContent() }
            }
            items(items = ayats, key = { it.remoteId }) { ayat ->
                QuranTranslationAyatItem(
                    ayat = ayat,
                    selected = ayat.remoteId == selectedAyatId,
                    onLongPress = { onAyatLongPress(ayat) },
                    onMore = { onAyatSelected(ayat) },
                    typography =
                        QuranAyatTypography(
                            arabicSizeSp = arabicSizeSp,
                            arabicLineHeightSp = arabicLineHeightSp,
                            translationSizeSp = translationSizeSp,
                            arabicFont = arabicFont,
                        ),
                )
            }
        }
    }
}

/** The three live-adjustable reader type settings plus the chosen face, bundled so the ayat row
 * keeps a short parameter list as the design adds elements to it. */
private data class QuranAyatTypography(
    val arabicSizeSp: Int,
    val arabicLineHeightSp: Int,
    val translationSizeSp: Int,
    val arabicFont: QuranArabicFont,
)

@Composable
private fun QuranTranslationAyatItem(
    ayat: QuranReaderAyatUiModel,
    selected: Boolean,
    onLongPress: () -> Unit,
    onMore: () -> Unit,
    typography: QuranAyatTypography,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val semanticsLabel = stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (selected) {
                        Modifier.background(QuranPrimaryContainer, RoundedCornerShape(SelectedAyatCornerRadius))
                    } else {
                        Modifier
                    },
                )
                .semantics {
                    onLongClick(label = semanticsLabel) {
                        onLongPress()
                        true
                    }
                }
                .pointerInput(ayat.remoteId) {
                    detectTapGestures(
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    )
                }
                .padding(vertical = AyatVerticalPadding),
    ) {
        QuranAyatMetaRow(ayat = ayat, selected = selected, onMore = onMore)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            // TextAlign.End resolves to the *left* edge once layout direction is Rtl (End is
            // logical, relative to direction) — Right is the physical alignment this needs.
            Text(
                text = ayat.arabicText.withQuranFontFallback(typography.arabicFont),
                style =
                    TextStyle(
                        fontFamily = typography.arabicFont.toFontFamily(),
                        fontSize = typography.arabicSizeSp.sp,
                        lineHeight = typography.arabicLineHeightSp.sp,
                        textAlign = TextAlign.Right,
                    ),
                color = QuranArabicText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = ayat.translation,
            color = QuranTranslationText,
            fontSize = typography.translationSizeSp.sp,
            lineHeight = (typography.translationSizeSp * TRANSLATION_LINE_HEIGHT_MULTIPLIER).sp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SanguSantriSpacing.medium),
        )
        QuranSourceAnnotations(ayat)
        HorizontalDivider(
            color = QuranOutline,
            modifier = Modifier.padding(top = AyatVerticalPadding),
        )
    }
}

/** The design's ayat header line: a tint circle holding the ayah number beside its juz/page origin,
 * with bookmark and overflow affordances trailing. `more_horiz` opens the same action sheet a
 * long-press does — a discoverable route to it for anyone who never tries long-pressing. */
@Composable
private fun QuranAyatMetaRow(
    ayat: QuranReaderAyatUiModel,
    selected: Boolean,
    onMore: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = SanguSantriSpacing.small),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(AyatNumberCircleSize)
                        .background(QuranPrimaryContainer, RoundedCornerShape(AyatNumberCircleSize / 2)),
            ) {
                Text(
                    text = ayat.ayatNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = QuranPrimary,
                )
            }
            Text(
                text = stringResource(R.string.quran_reader_ayat_origin, ayat.juz, ayat.page),
                style = MaterialTheme.typography.labelSmall,
                color = QuranMutedText,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = QuranMutedText,
                modifier = Modifier.size(AyatActionIconSize),
            )
            IconButton(onClick = onMore) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber),
                    tint = QuranMutedText,
                    modifier = Modifier.size(AyatActionIconSize),
                )
            }
        }
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = SanguSantriSpacing.small),
            )
        }
}

private val AyatVerticalPadding = 17.dp
private val AyatNumberCircleSize = 25.dp
private val AyatActionIconSize = 18.dp
private val SelectedAyatCornerRadius = 8.dp

private const val DEFAULT_ARABIC_SIZE_SP = QuranReaderSettings.DEFAULT_ARABIC_SIZE_SP
private const val DEFAULT_ARABIC_LINE_HEIGHT_SP = 65
private const val DEFAULT_TRANSLATION_SIZE_SP = QuranReaderSettings.DEFAULT_TRANSLATION_SIZE_SP
private const val TRANSLATION_LINE_HEIGHT_MULTIPLIER = 1.7f
