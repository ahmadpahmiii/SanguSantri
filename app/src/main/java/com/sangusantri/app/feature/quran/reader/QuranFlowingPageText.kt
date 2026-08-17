package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    textStyle: TextStyle =
        TextStyle(
            fontFamily = QuranLpmqFontFamily,
            fontSize = 30.sp,
            lineHeight = 50.sp,
            textAlign = TextAlign.Justify,
        ),
) {
    val arabicTextColor = QuranArabicText
    val annotatedPage = rememberQuranAnnotatedPage(ayats, selectedAyatId, arabicFont)
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    val accessibilityActions =
        ayats.map { ayat ->
            val label =
                androidx.compose.ui.res
                    .stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber)
            CustomAccessibilityAction(label) {
                onAyatLongPress(ayat)
                true
            }
        }

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

/** [buildPageText] runs outside composition, so the Light/Dark-aware colour roles it needs are
 * resolved here (composable context) and passed in as plain [Color] values — included in the
 * `remember` keys so a live [com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode]
 * toggle rebuilds the annotated string's baked-in span colours. */
@Composable
private fun rememberQuranAnnotatedPage(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    arabicFont: QuranArabicFont,
): AnnotatedString {
    val primaryColor = QuranPrimary
    val onPrimaryContainerColor = QuranOnPrimaryContainer
    val primaryContainerColor = QuranPrimaryContainer
    return remember(ayats, selectedAyatId, arabicFont, primaryColor, onPrimaryContainerColor, primaryContainerColor) {
        buildPageText(ayats, selectedAyatId, primaryColor, onPrimaryContainerColor, primaryContainerColor)
            .withQuranFontFallback(arabicFont)
    }
}

private fun buildPageText(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
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
            if (ayat.remoteId == selectedAyatId) {
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
