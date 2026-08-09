package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * Start-of-surah treatment for both reader modes. All labels are supplied by the Room-backed
 * presentation model. The separate basmalah is omitted for Al-Fatihah and At-Taubah.
 */
@Composable
fun QuranSurahStartHeader(
    surahNumber: Int,
    category: String,
    surahDisplayName: String,
    ayatCount: Int,
    modifier: Modifier = Modifier,
) {
    val headerDescription =
        stringResource(
            R.string.quran_surah_header_description,
            surahDisplayName,
            category,
            ayatCount,
        )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        QuranSurahMetadataBand(
            category = category,
            surahDisplayName = surahDisplayName,
            ayatCount = ayatCount,
            contentDescription = headerDescription,
        )
        if (surahNumber != AL_FATIHAH_NUMBER && surahNumber != AT_TAUBAH_NUMBER) {
            QuranBasmalah()
        }
    }
}

private const val AL_FATIHAH_NUMBER = 1
private const val AT_TAUBAH_NUMBER = 9

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
private fun QuranBasmalah() {
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
