package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranSurahHeaderVariant
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * Start-of-surah treatment for both reader modes. All labels are supplied by the Room-backed
 * presentation model. The separate basmalah is omitted for Al-Fatihah and At-Taubah.
 *
 * [QuranSurahHeaderVariant.TENANG] is the default (revamp handoff §4): the surah name set in the
 * same Arabic reading face as the text below, one muted caps line, a short hairline, then the
 * basmalah — also as text, in the reading face, not the drawable. [QuranSurahHeaderVariant.BAND]
 * keeps the previous three-column band and the basmalah drawable.
 *
 * [basmalahArabic] is the exact official Kemenag string for Al-Fatihah ayat 1, read from Room by
 * the ViewModel rather than hardcoded here — the app never carries its own copy of Quran Arabic.
 * When it is blank (dataset not prepared) no basmalah is drawn; nothing is substituted.
 */
@Suppress("LongParameterList")
@Composable
fun QuranSurahStartHeader(
    surahNumber: Int,
    category: String,
    surahDisplayName: String,
    surahArabicName: String,
    ayatCount: Int,
    basmalahArabic: String,
    modifier: Modifier = Modifier,
    variant: QuranSurahHeaderVariant = QuranSurahHeaderVariant.TENANG,
    arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    surahNameSizeSp: Int = TENANG_SURAH_NAME_SIZE_SP,
    basmalahSizeSp: Int = TENANG_BASMALAH_SIZE_SP,
) {
    val headerDescription =
        stringResource(
            R.string.quran_surah_header_description,
            surahDisplayName,
            category,
            ayatCount,
        )
    val showsBasmalah = surahNumber != AL_FATIHAH_NUMBER && surahNumber != AT_TAUBAH_NUMBER
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        when (variant) {
            QuranSurahHeaderVariant.TENANG ->
                QuranSurahTenangHeader(
                    category = category,
                    surahDisplayName = surahDisplayName,
                    surahArabicName = surahArabicName,
                    ayatCount = ayatCount,
                    basmalahArabic = if (showsBasmalah) basmalahArabic else "",
                    contentDescription = headerDescription,
                    arabicFont = arabicFont,
                    surahNameSizeSp = surahNameSizeSp,
                    basmalahSizeSp = basmalahSizeSp,
                )

            QuranSurahHeaderVariant.BAND -> {
                QuranSurahMetadataBand(
                    category = category,
                    surahDisplayName = surahDisplayName,
                    ayatCount = ayatCount,
                    contentDescription = headerDescription,
                )
                if (showsBasmalah) QuranBasmalahImage()
            }
        }
    }
}

private const val AL_FATIHAH_NUMBER = 1
private const val AT_TAUBAH_NUMBER = 9

// Handoff §4: surah name 33sp at line-height 1.95, caps line 11sp with 1.1px tracking, a 52x1dp
// hairline 18dp below it, then the basmalah at 27sp / line-height 2.2. The mushaf reader passes
// slightly smaller name/basmalah sizes (§5) through the two size parameters.
private const val TENANG_SURAH_NAME_SIZE_SP = 33
private const val TENANG_BASMALAH_SIZE_SP = 27
private const val TENANG_SURAH_NAME_LINE_HEIGHT = 1.95f
private const val TENANG_BASMALAH_LINE_HEIGHT = 2.2f
private val TenangHairlineWidth = 52.dp
private val TenangHairlineGap = 18.dp

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun QuranSurahTenangHeader(
    category: String,
    surahDisplayName: String,
    surahArabicName: String,
    ayatCount: Int,
    basmalahArabic: String,
    contentDescription: String,
    arabicFont: QuranArabicFont,
    surahNameSizeSp: Int,
    basmalahSizeSp: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SanguSantriDimensions.readerHorizontalPadding,
                    vertical = SanguSantriSpacing.medium,
                ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { this.contentDescription = contentDescription },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = surahArabicName.withQuranFontFallback(arabicFont),
                    style =
                        TextStyle(
                            fontFamily = arabicFont.toFontFamily(),
                            fontSize = surahNameSizeSp.sp,
                            lineHeight = (surahNameSizeSp * TENANG_SURAH_NAME_LINE_HEIGHT).sp,
                            textAlign = TextAlign.Center,
                        ),
                    color = QuranArabicText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.quran_surah_header_caps_line,
                        surahDisplayName.uppercase(),
                        category.uppercase(),
                        ayatCount,
                    ),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                color = QuranMutedText,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(TenangHairlineGap))
        Box(
            modifier =
                Modifier
                    .width(TenangHairlineWidth)
                    .height(1.dp)
                    .background(QuranOutline),
        )
        if (basmalahArabic.isNotBlank()) {
            Spacer(Modifier.height(TenangHairlineGap))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = basmalahArabic.withQuranFontFallback(arabicFont),
                    style =
                        TextStyle(
                            fontFamily = arabicFont.toFontFamily(),
                            fontSize = basmalahSizeSp.sp,
                            lineHeight = (basmalahSizeSp * TENANG_BASMALAH_LINE_HEIGHT).sp,
                            textAlign = TextAlign.Center,
                        ),
                    color = QuranArabicText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuranSurahMetadataBand(
    category: String,
    surahDisplayName: String,
    ayatCount: Int,
    contentDescription: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .padding(
                    start = SanguSantriSpacing.default,
                    top = SanguSantriSpacing.default,
                    end = SanguSantriSpacing.default,
                ),
    ) {
        Surface(
            color = QuranPrimaryContainer,
            contentColor = QuranOnPrimaryContainer,
            // design-export/quran/09-flowing-reader-arab-only-page.html `.surah-header{border:1px
            // solid color-mix(in srgb, var(--quran-primary) 42%, transparent)}` — a translucent
            // border, the band must stay subordinate to the Arabic reading text below it.
            border = BorderStroke(1.dp, QuranPrimary.copy(alpha = 0.42f)),
            shape = MaterialTheme.shapes.medium,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = SanguSantriDimensions.quranSurahHeaderMinHeight)
                    .clearAndSetSemantics { this.contentDescription = contentDescription },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.padding(
                        horizontal = SanguSantriSpacing.medium,
                        vertical = SanguSantriSpacing.extraSmall,
                    ),
            ) {
                HeaderSideLabel(category, Alignment.CenterStart, Modifier.weight(1f))
                QuranSurahTitlePill(surahDisplayName, Modifier.weight(1.2f))
                HeaderSideLabel(
                    text = stringResource(R.string.quran_surah_ayat_count, ayatCount),
                    alignment = Alignment.CenterEnd,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuranSurahTitlePill(
    surahDisplayName: String,
    modifier: Modifier = Modifier,
) {
    // design-export/quran/09-flowing-reader-arab-only-page.html `.surah-name` has no border —
    // it's a plain filled pill, distinct from the QuranNumberBadge/QuranSourceIcon border pattern
    // used elsewhere.
    Surface(
        color = QuranSurface,
        contentColor = QuranArabicText,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.heightIn(min = 40.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.small),
        ) {
            Text(
                text = surahDisplayName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuranBasmalahImage() {
    Image(
        painter = painterResource(R.drawable.quran_basmalah_simple),
        contentDescription = stringResource(R.string.quran_basmalah_content_description),
        colorFilter = ColorFilter.tint(QuranArabicText),
        modifier =
            Modifier
                .padding(top = SanguSantriSpacing.large)
                .padding(bottom = SanguSantriSpacing.small)
                .widthIn(max = SanguSantriDimensions.quranBasmalahMaxWidth)
                .fillMaxWidth(),
    )
}

@Composable
private fun HeaderSideLabel(
    text: String,
    alignment: Alignment,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = alignment, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = QuranOnPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
