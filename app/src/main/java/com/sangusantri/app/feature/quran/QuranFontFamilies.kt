package com.sangusantri.app.feature.quran

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.QuranArabicFont

val QuranLpmqFontFamily = FontFamily(Font(R.font.lpmq_isep_misbah))
val QuranAmiriFontFamily = FontFamily(Font(R.font.amiri_quran_regular))

fun QuranArabicFont.toFontFamily(): FontFamily =
    when (this) {
        QuranArabicFont.LPMQ_ISEP_MISBAH -> QuranLpmqFontFamily
        QuranArabicFont.AMIRI_QURAN -> QuranAmiriFontFamily
    }

/**
 * Keeps the selected Amiri face for the corpus it supports, while rendering the complete word in
 * the source-paired LPMQ face when that word contains one of the three Kemenag-corpus code points
 * absent from the packaged Amiri Quran binary. The source [AnnotatedString] is appended unchanged;
 * only presentation spans are added, so Quran text and existing ayat annotations stay intact.
 */
fun AnnotatedString.withQuranFontFallback(font: QuranArabicFont): AnnotatedString {
    val sourceText = text
    if (font != QuranArabicFont.AMIRI_QURAN || sourceText.none { it.code in AMIRI_MISSING_CODE_POINTS }) return this

    return buildAnnotatedString {
        append(this@withQuranFontFallback)
        sourceText.indices
            .filter { sourceText[it].code in AMIRI_MISSING_CODE_POINTS }
            .map { index ->
                val start = (index - 1 downTo 0).firstOrNull { sourceText[it].isWhitespace() }?.plus(1) ?: 0
                val end =
                    (index + 1 until sourceText.length)
                        .firstOrNull { sourceText[it].isWhitespace() }
                        ?: sourceText.length
                start until end
            }.distinct()
            .forEach { range -> addStyle(SpanStyle(fontFamily = QuranLpmqFontFamily), range.first, range.last + 1) }
    }
}

fun String.withQuranFontFallback(font: QuranArabicFont): AnnotatedString =
    AnnotatedString(withQuranPresentationSpacing()).withQuranFontFallback(font)

/**
 * Prevents a source-separated Quran annotation from wrapping onto a line by itself. Replacing the
 * preceding regular space with a non-breaking space is presentation-only and length-preserving;
 * Room and domain values remain the exact Kemenag strings.
 */
fun String.withQuranPresentationSpacing(): String {
    if (none { it.code in AMIRI_MISSING_CODE_POINTS }) return this

    val displayText = toCharArray()
    indices
        .filter { this[it].code in AMIRI_MISSING_CODE_POINTS && it > 0 && this[it - 1] == ' ' }
        .forEach { markIndex -> displayText[markIndex - 1] = NON_BREAKING_SPACE }
    return displayText.concatToString()
}

private val AMIRI_MISSING_CODE_POINTS = setOf(0x06D4, 0x06D5, 0x08D6)
private const val NON_BREAKING_SPACE = '\u00A0'
