package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback

/**
 * Start-of-surah treatment for both reader modes: the surah name set in the same Arabic reading
 * face as the text below, one muted caps line, a short hairline, then the basmalah — also as text
 * in the reading face. All labels are supplied by the Room-backed presentation model. The separate
 * basmalah is omitted for Al-Fatihah and At-Taubah.
 *
 * [basmalahArabic] is the exact official Kemenag string for Al-Fatihah ayat 1, read from Room by
 * the ViewModel rather than hardcoded here — the app never carries its own copy of Quran Arabic.
 * When it is blank (dataset not prepared) no basmalah is drawn; nothing is substituted.
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun QuranSurahStartHeader(
    surahNumber: Int,
    category: String,
    surahDisplayName: String,
    surahArabicName: String,
    ayatCount: Int,
    basmalahArabic: String,
    modifier: Modifier = Modifier,
    arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    surahNameSizeSp: Int = SURAH_NAME_SIZE_SP,
    basmalahSizeSp: Int = BASMALAH_SIZE_SP,
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
        modifier =
            modifier
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
                    .clearAndSetSemantics { this.contentDescription = headerDescription },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = surahArabicName.withQuranFontFallback(arabicFont),
                    style =
                        TextStyle(
                            fontFamily = arabicFont.toFontFamily(),
                            fontSize = surahNameSizeSp.sp,
                            lineHeight = (surahNameSizeSp * SURAH_NAME_LINE_HEIGHT).sp,
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
        Spacer(Modifier.height(HairlineGap))
        Box(
            modifier =
                Modifier
                    .width(HairlineWidth)
                    .height(1.dp)
                    .background(QuranOutline),
        )
        if (showsBasmalah && basmalahArabic.isNotBlank()) {
            Spacer(Modifier.height(HairlineGap))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = basmalahArabic.withQuranFontFallback(arabicFont),
                    style =
                        TextStyle(
                            fontFamily = arabicFont.toFontFamily(),
                            fontSize = basmalahSizeSp.sp,
                            lineHeight = (basmalahSizeSp * BASMALAH_LINE_HEIGHT).sp,
                            textAlign = TextAlign.Center,
                        ),
                    color = QuranArabicText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private const val AL_FATIHAH_NUMBER = 1
private const val AT_TAUBAH_NUMBER = 9

// Handoff §4: surah name 33sp at line-height 1.95, caps line 11sp with 1.1px tracking, a 52x1dp
// hairline 18dp below it, then the basmalah at 27sp / line-height 2.2. The mushaf reader passes
// slightly smaller name/basmalah sizes (§5) through the two size parameters.
private const val SURAH_NAME_SIZE_SP = 33
private const val BASMALAH_SIZE_SP = 27
private const val SURAH_NAME_LINE_HEIGHT = 1.95f
private const val BASMALAH_LINE_HEIGHT = 2.2f
private val HairlineWidth = 52.dp
private val HairlineGap = 18.dp
