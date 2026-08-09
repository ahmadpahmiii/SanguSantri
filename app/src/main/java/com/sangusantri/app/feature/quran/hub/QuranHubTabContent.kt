package com.sangusantri.app.feature.quran.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.feature.quran.QuranLpmqFontFamily

/** Dispatches to the selected tab's list — split out of `QuranHubScreen.kt` to keep both files
 * under the project's function-count threshold. */
@Composable
fun QuranHubTabContent(
    uiState: QuranHubUiState,
    actions: QuranHubActions,
) {
    when (uiState.selectedTab) {
        QuranHubTab.SURAH -> QuranSurahList(surahs = uiState.surahs, onSurahSelected = actions.onSurahSelected)
        QuranHubTab.JUZ -> QuranJuzList(rows = uiState.juzRows, onAyatSelected = actions.onAyatSelected)
        QuranHubTab.BOOKMARK -> QuranBookmarkList(rows = uiState.bookmarkRows, onAyatSelected = actions.onAyatSelected)
    }
}

@Composable
private fun QuranSurahList(
    surahs: List<QuranSurah>,
    onSurahSelected: (Int) -> Unit,
) {
    if (surahs.isEmpty()) {
        QuranEmptyTabState(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            description = stringResource(R.string.quran_hub_surah_empty),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = surahs, key = { it.number }) { surah ->
            QuranSurahRow(surah = surah, onClick = { onSurahSelected(surah.number) })
            HorizontalDivider(color = QuranOutline)
        }
    }
}

@Composable
private fun QuranSurahRow(
    surah: QuranSurah,
    onClick: () -> Unit,
) {
    QuranListRow(onClick = onClick) {
        QuranNumberBadge(number = surah.number)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.latinName,
                style = MaterialTheme.typography.titleMedium,
                color = QuranArabicText,
            )
            Text(
                text =
                    stringResource(
                        R.string.quran_hub_surah_meaning_category_and_count,
                        surah.meaning,
                        surah.category,
                        surah.ayatCount,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
                maxLines = 1,
            )
        }
        Text(
            text = surah.arabicName,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = QuranLpmqFontFamily,
            color = QuranPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.7f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = QuranMutedText,
        )
    }
}

@Composable
private fun QuranJuzList(
    rows: List<QuranJuzRow>,
    onAyatSelected: (Int, Int) -> Unit,
) {
    if (rows.isEmpty()) {
        QuranEmptyTabState(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            description = stringResource(R.string.quran_hub_juz_empty),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = rows, key = { it.juzNumber }) { row ->
            QuranListRow(onClick = { onAyatSelected(row.surahNumber, row.ayatNumber) }) {
                QuranNumberBadge(number = row.juzNumber)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quran_hub_juz_title, row.juzNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = QuranArabicText,
                    )
                    Text(
                        text = stringResource(R.string.quran_hub_juz_position, row.surahName, row.ayatNumber, row.page),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranMutedText,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = QuranMutedText,
                )
            }
            HorizontalDivider(color = QuranOutline)
        }
    }
}

@Composable
private fun QuranBookmarkList(
    rows: List<QuranBookmarkRow>,
    onAyatSelected: (Int, Int) -> Unit,
) {
    if (rows.isEmpty()) {
        QuranEmptyTabState(
            icon = Icons.Outlined.BookmarkBorder,
            title = stringResource(R.string.quran_hub_bookmark_empty_title),
            description = stringResource(R.string.quran_hub_bookmark_empty),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = rows, key = { "${it.surahNumber}:${it.ayatNumber}" }) { row ->
            QuranListRow(onClick = { onAyatSelected(row.surahNumber, row.ayatNumber) }) {
                Icon(imageVector = Icons.Outlined.Bookmark, contentDescription = null, tint = QuranPrimary)
                Column(
                    modifier =
                        Modifier
                            .padding(start = SanguSantriSpacing.small)
                            .weight(1f),
                ) {
                    Text(text = row.surahName, style = MaterialTheme.typography.titleMedium, color = QuranArabicText)
                    Text(
                        text = stringResource(R.string.quran_hub_bookmark_position, row.ayatNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranMutedText,
                    )
                }
            }
            HorizontalDivider(color = QuranOutline)
        }
    }
}

@Composable
private fun QuranListRow(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                .clickable(onClick = onClick)
                .padding(horizontal = SanguSantriSpacing.small, vertical = SanguSantriSpacing.medium),
        content = content,
    )
}

@Composable
private fun QuranNumberBadge(number: Int) {
    Surface(
        color = QuranSurface,
        contentColor = QuranPrimary,
        border = BorderStroke(1.dp, QuranOutline),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.size(42.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = number.toString(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Matches the design's `.empty` pattern (icon mark, optional heading, description) — see
 * figma-export/quran/03b-quran-hub-bookmark-empty.html; reused for the Surah/Juz empty
 * fallbacks, which have no dedicated design frame since Room is always populated by then. */
@Composable
private fun QuranEmptyTabState(
    icon: ImageVector,
    description: String,
    title: String? = null,
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SanguSantriSpacing.large),
        ) {
            Surface(
                color = QuranSurface,
                contentColor = QuranPrimary,
                border = BorderStroke(1.dp, QuranOutline),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.size(SanguSantriDimensions.quranEmptyStateMarkSize),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = QuranArabicText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = SanguSantriSpacing.default),
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(top = if (title != null) SanguSantriSpacing.small else SanguSantriSpacing.default)
                        .widthIn(max = SanguSantriDimensions.quranEmptyStateDescriptionMaxWidth),
            )
        }
    }
}
