package com.sangusantri.app.feature.nahwuquiz

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
import androidx.compose.material.icons.filled.Quiz
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
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NahwuQuizHistoryRoute(
    packageId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizHistoryViewModel =
        hiltViewModel<NahwuQuizHistoryViewModel, NahwuQuizHistoryViewModel.Factory>(
            creationCallback = { factory -> factory.create(packageId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizHistoryScreen(uiState = uiState, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizHistoryScreen(
    uiState: NahwuQuizHistoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nahwu_quiz_history_title)) },
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
            NahwuQuizHistoryUiState.Loading ->
                NahwuQuizLoadingState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            NahwuQuizHistoryUiState.Empty ->
                NahwuQuizHistoryEmptyState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            is NahwuQuizHistoryUiState.Filled ->
                NahwuQuizHistoryList(
                    attempts = uiState.attempts,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun NahwuQuizHistoryEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Quiz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.nahwu_quiz_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SanguSantriSpacing.default),
            )
        }
    }
}

@Composable
private fun NahwuQuizHistoryList(
    attempts: List<NahwuQuizAttempt>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(horizontal = SanguSantriSpacing.default)) {
        items(items = attempts, key = { it.id }) { attempt ->
            NahwuQuizHistoryRow(attempt = attempt)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun NahwuQuizHistoryRow(
    attempt: NahwuQuizAttempt,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SanguSantriSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.nahwu_quiz_history_row_score, attempt.scorePercent),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.nahwu_quiz_history_row_correct_total,
                        attempt.correctCount,
                        attempt.totalCount,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            attempt.completedAtEpochMillis?.let { completedAt ->
                Text(text = formatDate(completedAt), style = MaterialTheme.typography.bodyMedium)
            }
            attempt.durationMillis?.let { duration ->
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val historyDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

private fun formatDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(historyDateFormatter)

@Composable
private fun formatDuration(durationMillis: Long): String {
    val minutes = durationMillis / MILLIS_PER_MINUTE
    return if (minutes < 1) {
        stringResource(R.string.nahwu_quiz_history_row_duration_less_than_minute)
    } else {
        stringResource(R.string.nahwu_quiz_history_row_duration_minutes, minutes)
    }
}

private const val MILLIS_PER_MINUTE = 60_000L

@PreviewLightDark
@Composable
private fun NahwuQuizHistoryScreenEmptyPreview() {
    SanguSantriTheme {
        NahwuQuizHistoryScreen(uiState = NahwuQuizHistoryUiState.Empty, onBack = {})
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizHistoryScreenFilledPreview() {
    SanguSantriTheme {
        NahwuQuizHistoryScreen(
            uiState =
                NahwuQuizHistoryUiState.Filled(
                    packageTitle = "[FIXTURE] Nahwu Dasar",
                    attempts =
                        listOf(
                            NahwuQuizAttempt(
                                id = "1",
                                packageId = "nahwu-dasar-fixture",
                                startedAtEpochMillis = 0,
                                completedAtEpochMillis = 5 * MILLIS_PER_MINUTE,
                                currentQuestionIndex = 6,
                                correctCount = 5,
                                totalCount = 6,
                            ),
                            NahwuQuizAttempt(
                                id = "2",
                                packageId = "nahwu-dasar-fixture",
                                startedAtEpochMillis = 0,
                                completedAtEpochMillis = 8 * MILLIS_PER_MINUTE,
                                currentQuestionIndex = 6,
                                correctCount = 4,
                                totalCount = 6,
                            ),
                        ),
                ),
            onBack = {},
        )
    }
}
