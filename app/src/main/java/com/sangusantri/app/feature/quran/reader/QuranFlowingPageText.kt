package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.QuranLpmqFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback
import com.sangusantri.app.feature.quran.withQuranPresentationSpacing
import java.text.NumberFormat
import java.util.Locale

private const val AYAT_ANNOTATION_TAG = "quran-ayat-id"

/**
 * Responsive Arab-only page rendering. It preserves each official Arabic source string verbatim,
 * adding only a presentation-space and a marker derived from the official numeric ayat metadata.
 */
@Composable
@Suppress("LongParameterList")
fun QuranFlowingPageText(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    /** The ayah being recited, highlighted inline within the flowing paragraph (`4f`). */
    playingAyatNumber: Int? = null,
    /**
     * Reports where an ayah starts vertically inside this page, in pixels from the text's own top,
     * once it has been measured. Called only by the page that actually holds [playingAyatNumber], and
     * only with a measured result — pages without that ayah stay silent rather than reporting `null`,
     * so several composed pages cannot overwrite each other.
     *
     * The ayah number is reported alongside the offset because the two must travel together: a caller
     * holding an offset without knowing which ayah it belongs to will happily apply the previous
     * ayah's position to the next page and scroll far too far.
     *
     * This is what lets follow-scrolling stay accurate at any Arabic size: the offset comes from the
     * real [TextLayoutResult], so a page three screens tall at 52sp positions its ayat as precisely as
     * a short one at 14sp.
     */
    onPlayingAyatOffset: (ayatNumber: Int, offsetPx: Float) -> Unit = { _, _ -> },
    textStyle: TextStyle =
        TextStyle(
            fontFamily = QuranLpmqFontFamily,
            fontSize = 30.sp,
            lineHeight = 50.sp,
            textAlign = TextAlign.Justify,
        ),
) {
    val arabicTextColor = QuranArabicText
    val annotatedPage = rememberQuranAnnotatedPage(ayats, selectedAyatId, arabicFont, playingAyatNumber)
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    QuranReportPlayingAyatOffset(
        annotatedPage = annotatedPage,
        layout = textLayoutResult,
        ayats = ayats,
        playingAyatNumber = playingAyatNumber,
        onPlayingAyatOffset = onPlayingAyatOffset,
    )
    val hapticFeedback = LocalHapticFeedback.current
    val accessibilityActions = rememberQuranAyatAccessibilityActions(ayats, onAyatLongPress)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BasicText(
            text = annotatedPage,
            style = textStyle,
            color = { arabicTextColor },
            onTextLayout = { textLayoutResult = it },
            modifier =
                modifier
                    .fillMaxWidth()
                    .semantics { customActions = accessibilityActions }
                    .pointerInput(annotatedPage, ayats, onTap) {
                        detectTapGestures(
                            // Mushaf immersion (handoff §5): one tap clears the chrome for a clean
                            // page, another restores it.
                            onTap = { onTap() },
                            onLongPress = { position ->
                                val layout = textLayoutResult ?: return@detectTapGestures
                                if (annotatedPage.isEmpty()) return@detectTapGestures
                                val offset =
                                    layout
                                        .getOffsetForPosition(position)
                                        .coerceIn(0, annotatedPage.lastIndex)
                                val remoteId =
                                    annotatedPage
                                        .getStringAnnotations(
                                            tag = AYAT_ANNOTATION_TAG,
                                            start = offset,
                                            end = (offset + 1).coerceAtMost(annotatedPage.length),
                                        ).firstOrNull()
                                        ?.item
                                        ?.toLongOrNull()
                                val ayat = ayats.firstOrNull { it.remoteId == remoteId }
                                if (ayat != null) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAyatLongPress(ayat)
                                }
                            },
                        )
                    },
        )
    }
}

/** One "open ayat N" action per ayah on the page, so a screen reader can reach every ayah's actions
 * without depending on a long-press landing on the right glyph in a single flowing paragraph. */
@Composable
private fun rememberQuranAyatAccessibilityActions(
    ayats: List<QuranReaderAyatUiModel>,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
): List<CustomAccessibilityAction> =
    ayats.map { ayat ->
        val label = stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber)
        CustomAccessibilityAction(label) {
            onAyatLongPress(ayat)
            true
        }
    }

/** Publishes the recited ayah's measured position, re-running when the layout changes so adjusting the
 * Arabic size or line spacing mid-recitation immediately yields a fresh, correct offset. Silent unless
 * this page holds that ayah and the text has been measured. */
@Composable
private fun QuranReportPlayingAyatOffset(
    annotatedPage: AnnotatedString,
    layout: TextLayoutResult?,
    ayats: List<QuranReaderAyatUiModel>,
    playingAyatNumber: Int?,
    onPlayingAyatOffset: (ayatNumber: Int, offsetPx: Float) -> Unit,
) {
    LaunchedEffect(layout, playingAyatNumber, annotatedPage) {
        val ayat = playingAyatNumber ?: return@LaunchedEffect
        val offset = annotatedPage.ayatTopOffset(layout, ayats, ayat) ?: return@LaunchedEffect
        onPlayingAyatOffset(ayat, offset)
    }
}

/**
 * Vertical position of [playingAyatNumber]'s first line within this measured page, or `null` when the
 * ayah is not on this page or the text has not been laid out yet.
 *
 * The ayah's character range is already recorded as an annotation for long-press hit-testing, so the
 * same annotation answers "where does this ayah begin" — no second index of positions to keep in step
 * with the text.
 */
private fun AnnotatedString.ayatTopOffset(
    layout: TextLayoutResult?,
    ayats: List<QuranReaderAyatUiModel>,
    playingAyatNumber: Int?,
): Float? {
    val resolvedLayout = layout ?: return null
    return ayats
        .firstOrNull { it.ayatNumber == playingAyatNumber }
        ?.let { ayat ->
            getStringAnnotations(AYAT_ANNOTATION_TAG, 0, length)
                .firstOrNull { it.item == ayat.remoteId.toString() }
        }?.let { annotation ->
            runCatching { resolvedLayout.getLineTop(resolvedLayout.getLineForOffset(annotation.start)) }.getOrNull()
        }
}

/** [buildPageText] runs outside composition, so the Light/Dark-aware colour roles it needs are
 * resolved here (composable context) and passed in as plain [Color] values — included in the
 * `remember` keys so a live [com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode]
 * toggle rebuilds the annotated string's baked-in span colours. */
@Composable
private fun rememberQuranAnnotatedPage(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    arabicFont: QuranArabicFont,
    playingAyatNumber: Int?,
): AnnotatedString {
    val primaryColor = QuranPrimary
    val onPrimaryContainerColor = QuranOnPrimaryContainer
    val primaryContainerColor = QuranPrimaryContainer
    return remember(
        ayats,
        selectedAyatId,
        arabicFont,
        playingAyatNumber,
        primaryColor,
        onPrimaryContainerColor,
        primaryContainerColor,
    ) {
        buildPageText(
            ayats,
            selectedAyatId,
            playingAyatNumber,
            primaryColor,
            onPrimaryContainerColor,
            primaryContainerColor,
        ).withQuranFontFallback(arabicFont)
    }
}

@Suppress("LongParameterList")
private fun buildPageText(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    playingAyatNumber: Int?,
    primaryColor: Color,
    onPrimaryContainerColor: Color,
    primaryContainerColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        val arabicNumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("ar"))
        ayats.forEachIndexed { index, ayat ->
            if (index > 0) append(' ')
            val rangeStart = length
            pushStringAnnotation(AYAT_ANNOTATION_TAG, ayat.remoteId.toString())
            append(ayat.arabicText.withQuranPresentationSpacing())
            append(" ﴿")
            append(arabicNumberFormat.format(ayat.ayatNumber))
            append("﴾")
            pop()
            val rangeEnd = length
            addStyle(
                SpanStyle(color = primaryColor),
                rangeEnd - arabicNumberFormat.format(ayat.ayatNumber).length - 2,
                rangeEnd,
            )
            // Selection and recitation share the tint: only one of them applies to a given ayah at a
            // time in practice, and the design gives both the same treatment inside the paragraph.
            if (ayat.remoteId == selectedAyatId || ayat.ayatNumber == playingAyatNumber) {
                addStyle(
                    SpanStyle(
                        color = onPrimaryContainerColor,
                        background = primaryContainerColor,
                    ),
                    rangeStart,
                    rangeEnd,
                )
            }
        }
    }
