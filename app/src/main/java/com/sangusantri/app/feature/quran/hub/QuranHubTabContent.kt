package com.sangusantri.app.feature.quran.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranSurah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        QuranHubTab.TERAKHIR_DIBACA ->
            QuranRecentSessionList(rows = uiState.recentSessionRows, onAyatSelected = actions.onAyatSelected)
    }
}

@Composable
private fun QuranSurahList(
    surahs: List<QuranSurah>,
    onSurahSelected: (Int) -> Unit,
) {
    if (surahs.isEmpty()) {
        QuranEmptyTabState(message = stringResource(R.string.quran_hub_surah_empty))
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quran_hub_surah_number_and_name, surah.number, surah.latinName),
                style = MaterialTheme.typography.titleMedium,
                color = QuranArabicText,
            )
            Text(
                text = stringResource(R.string.quran_hub_surah_category_and_count, surah.category, surah.ayatCount),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
            )
        }
        Text(text = surah.arabicName, style = MaterialTheme.typography.titleMedium, color = QuranArabicText)
    }
}

@Composable
private fun QuranJuzList(
    rows: List<QuranJuzRow>,
    onAyatSelected: (Int, Int) -> Unit,
) {
    if (rows.isEmpty()) {
        QuranEmptyTabState(message = stringResource(R.string.quran_hub_juz_empty))
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = rows, key = { it.juzNumber }) { row ->
            QuranListRow(onClick = { onAyatSelected(row.surahNumber, row.ayatNumber) }) {
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
        QuranEmptyTabState(message = stringResource(R.string.quran_hub_bookmark_empty))
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = rows, key = { "${it.surahNumber}:${it.ayatNumber}" }) { row ->
            QuranListRow(onClick = { onAyatSelected(row.surahNumber, row.ayatNumber) }) {
                Icon(imageVector = Icons.Outlined.Bookmark, contentDescription = null, tint = QuranPrimary)
                Column(modifier = Modifier
                    .padding(start = SanguSantriSpacing.small)
                    .weight(1f)) {
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
private fun QuranRecentSessionList(
    rows: List<QuranRecentSessionRow>,
    onAyatSelected: (Int, Int) -> Unit,
) {
    if (rows.isEmpty()) {
        QuranEmptyTabState(message = stringResource(R.string.quran_hub_recent_empty))
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = rows, key = { it.readAtEpochMillis }) { row ->
            QuranListRow(onClick = { onAyatSelected(row.surahNumber, row.endAyat) }) {
                Icon(imageVector = Icons.Outlined.History, contentDescription = null, tint = QuranMutedText)
                Column(modifier = Modifier
                    .padding(start = SanguSantriSpacing.small)
                    .weight(1f)) {
                    Text(text = row.surahName, style = MaterialTheme.typography.titleMedium, color = QuranArabicText)
                    Text(
                        text = stringResource(R.string.quran_hub_recent_position, row.startAyat, row.endAyat),
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranMutedText,
                    )
                }
                Text(
                    text = formatRelativeTimestamp(row.readAtEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranMutedText,
                )
            }
            HorizontalDivider(color = QuranOutline)
        }
    }
}

private fun formatRelativeTimestamp(epochMillis: Long): String =
    SimpleDateFormat("d MMM", Locale.forLanguageTag("id-ID")).format(Date(epochMillis))

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
                .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
        content = content,
    )
}

@Composable
private fun QuranEmptyTabState(message: String) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(QuranBackground), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(SanguSantriSpacing.large),
        )
    }
}
