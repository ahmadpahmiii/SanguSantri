package com.sangusantri.app.feature.quran

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.QuranArabicFont

val QuranLpmqFontFamily = FontFamily(Font(R.font.lpmq_isep_misbah))
val QuranHafsFontFamily = FontFamily(Font(R.font.kfgqpc_hafs_uthmanic))
val QuranAmiriFontFamily = FontFamily(Font(R.font.amiri_quran_regular))

fun QuranArabicFont.toFontFamily(): FontFamily = when (this) {
    QuranArabicFont.LPMQ_ISEP_MISBAH -> QuranLpmqFontFamily
    QuranArabicFont.KFGQPC_HAFS -> QuranHafsFontFamily
    QuranArabicFont.AMIRI_QURAN -> QuranAmiriFontFamily
}

/**
 * Keeps the selected face for the corpus it supports, while rendering the complete word in the
 * source-paired LPMQ face when that word contains a code point the selected face has no glyph for.
 * The source [AnnotatedString] is appended unchanged; only presentation spans are added, so Quran
 * text and existing ayat annotations stay intact.
 */
fun AnnotatedString.withQuranFontFallback(font: QuranArabicFont): AnnotatedString {
    val missing = FONT_MISSING_CODE_POINTS[font].orEmpty()
    val sourceText = text
    if (sourceText.none { it.code in missing }) return this

    return buildAnnotatedString {
        append(this@withQuranFontFallback)
        sourceText.indices
            .filter { sourceText[it].code in missing }
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
    if (none { it.code in SPACE_SEPARATED_MARKS }) return this

    val displayText = toCharArray()
    indices
        .filter { this[it].code in SPACE_SEPARATED_MARKS && it > 0 && this[it - 1] == ' ' }
        .forEach { markIndex -> displayText[markIndex - 1] = NON_BREAKING_SPACE }
    return displayText.concatToString()
}

/**
 * Code points each packaged face cannot render **correctly** — measured with fontTools against the
 * shipped binary and the Arabic this app actually shows: all 6236 stored Kemenag ayat plus every
 * bundled amaliyah/sholawat `content_steps.arabicText`. One choice drives all of them, so both
 * corpora count. LPMQ Isep Misbah renders both completely, which is why it is the default, is the
 * fallback face, and has no entry here.
 *
 * "Cannot render correctly" is two failures, not one, and the second is the easy one to miss:
 *
 * 1. **No glyph** — the code point is absent from the face's `cmap`, and renders as tofu.
 * 2. **An unpositioned mark** — the glyph exists, but the face gives a Unicode combining mark
 *    (`Mn`) either a non-mark GDEF class or no `MarkBasePos`/`MarkMarkPos` anchor, so HarfBuzz
 *    draws it at the pen position instead of over its base letter. It looks like a glyph on the
 *    baseline (KFGQPC HAFS' U+06E4) or a blot on top of the word (KFGQPC Nastaleeq's, which is why
 *    that face is not packaged). A `cmap` check alone passes both of those.
 *
 * Re-measure — do not guess — whenever a font file changes or synced content introduces a code
 * point outside these corpora. Both checks matter: cmap coverage, then GDEF class and GPOS mark
 * coverage for every `Mn` code point the corpora use.
 */
private val FONT_MISSING_CODE_POINTS =
    mapOf(
        // No glyph: U+0622 alef madda (14), U+08D6 small high ain (562), U+08D9 (1). Marks the face
        // carries as spacing base glyphs rather than marks, so they land on the baseline:
        // U+06E4 small high madda (2099), U+06DF (27), U+06E3 (3), U+06EB (1).
        QuranArabicFont.KFGQPC_HAFS to setOf(0x0622, 0x06DF, 0x06E3, 0x06E4, 0x06EB, 0x08D6, 0x08D9),
        // No glyph: U+06D4 full stop (29), U+06D5 ae (98), U+08D6 (562), U+08D9 (1). Amiri anchors
        // every mark the corpora use, so it has no second-category entry.
        QuranArabicFont.AMIRI_QURAN to setOf(0x06D4, 0x06D5, 0x08D6, 0x08D9),
    )

/** Corpus marks the Kemenag text writes as their own space-separated token, for every font. */
private val SPACE_SEPARATED_MARKS = setOf(0x06D4, 0x06D5, 0x08D6)
private const val NON_BREAKING_SPACE = '\u00A0'
