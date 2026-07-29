package com.sangusantri.app.feature.tasbih.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.icon.TasbihIcon
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TasbihHistoryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasbihHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TasbihHistoryScreen(uiState = uiState, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihHistoryScreen(
    uiState: TasbihHistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tasbih_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            TasbihHistoryUiState.Loading ->
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            TasbihHistoryUiState.Empty ->
                TasbihHistoryEmptyState(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )

            is TasbihHistoryUiState.Filled ->
                TasbihHistoryList(entries = uiState.entries, modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())
        }
    }
}

@Composable
private fun TasbihHistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(SanguSantriSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.tasbih_history_empty_heading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = SanguSantriSpacing.default),
        )
        Text(
            text = stringResource(R.string.tasbih_history_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
        )
    }
}

@Composable
private fun TasbihHistoryList(
    entries: List<TasbihHistoryEntry>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = SanguSantriSpacing.default),
    ) {
        items(items = entries, key = { it.id }) { entry ->
            TasbihHistoryRow(entry = entry)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun TasbihHistoryRow(
    entry: TasbihHistoryEntry,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SanguSantriSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TasbihIcon(filled = true, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.sessionName ?: stringResource(R.string.tasbih_history_row_name_default),
                style = MaterialTheme.typography.titleMedium,
            )
            val targetText =
                entry.targetValue?.toString() ?: stringResource(R.string.tasbih_target_unlimited_short)
            Text(
                text = stringResource(R.string.tasbih_history_row_target, targetText, entry.finalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = formatEndTime(entry.endedAtEpochMillis), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatDuration(entry.startedAtEpochMillis, entry.endedAtEpochMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val historyTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatEndTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(historyTimeFormatter)

@Composable
private fun formatDuration(
    startedAtEpochMillis: Long,
    endedAtEpochMillis: Long,
): String {
    val minutes = (endedAtEpochMillis - startedAtEpochMillis) / MILLIS_PER_MINUTE
    return if (minutes < 1) {
        stringResource(R.string.tasbih_history_row_duration_less_than_minute)
    } else {
        stringResource(R.string.tasbih_history_row_duration_minutes, minutes)
    }
}

private const val MILLIS_PER_MINUTE = 60_000L

@PreviewLightDark
@Composable
private fun TasbihHistoryScreenEmptyPreview() {
    SanguSantriTheme {
        TasbihHistoryScreen(uiState = TasbihHistoryUiState.Empty, onBack = {})
    }
}

@PreviewLightDark
@Composable
private fun TasbihHistoryScreenFilledPreview() {
    SanguSantriTheme {
        TasbihHistoryScreen(
            uiState =
                TasbihHistoryUiState.Filled(
                    entries =
                        listOf(
                            TasbihHistoryEntry(
                                id = 1,
                                sessionName = "Tahlil malam Jumat",
                                targetValue = 33,
                                finalCount = 33,
                                startedAtEpochMillis = 0,
                                endedAtEpochMillis = 5 * MILLIS_PER_MINUTE,
                            ),
                            TasbihHistoryEntry(
                                id = 2,
                                sessionName = null,
                                targetValue = null,
                                finalCount = 214,
                                startedAtEpochMillis = 0,
                                endedAtEpochMillis = 12 * MILLIS_PER_MINUTE,
                            ),
                        ),
                ),
            onBack = {},
        )
    }
}
