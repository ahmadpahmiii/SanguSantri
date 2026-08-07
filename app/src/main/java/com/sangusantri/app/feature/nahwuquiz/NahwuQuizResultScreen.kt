package com.sangusantri.app.feature.nahwuquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState
import kotlin.math.abs

@Composable
fun NahwuQuizResultRoute(
    attemptId: String,
    onViewHistory: (packageId: String) -> Unit,
    onRetakeQuiz: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizResultViewModel =
        hiltViewModel<NahwuQuizResultViewModel, NahwuQuizResultViewModel.Factory>(
            creationCallback = { factory -> factory.create(attemptId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizResultScreen(
        uiState = uiState,
        onViewHistory = onViewHistory,
        onRetakeQuiz = onRetakeQuiz,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizResultScreen(
    uiState: NahwuQuizResultUiState,
    onViewHistory: (packageId: String) -> Unit,
    onRetakeQuiz: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.nahwu_quiz_result_title)) }) },
    ) { innerPadding ->
        when (uiState) {
            NahwuQuizResultUiState.Loading, NahwuQuizResultUiState.NotFound ->
                NahwuQuizLoadingState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            is NahwuQuizResultUiState.Content ->
                ResultContent(
                    uiState = uiState,
                    onViewHistory = onViewHistory,
                    onRetakeQuiz = onRetakeQuiz,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun ResultContent(
    uiState: NahwuQuizResultUiState.Content,
    onViewHistory: (packageId: String) -> Unit,
    onRetakeQuiz: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SanguSantriSpacing.default),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        Text(text = uiState.packageTitle, style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.nahwu_quiz_result_score_percent, uiState.scorePercent),
            style = MaterialTheme.typography.displayMedium,
        )
        Text(
            text = stringResource(R.string.nahwu_quiz_result_correct_total, uiState.correctCount, uiState.totalCount),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        uiState.previousScorePercent?.let { previous ->
            val delta = uiState.scorePercent - previous
            val deltaRes =
                when {
                    delta > 0 -> R.string.nahwu_quiz_result_delta_up
                    delta < 0 -> R.string.nahwu_quiz_result_delta_down
                    else -> R.string.nahwu_quiz_result_delta_same
                }
            Text(
                text = stringResource(deltaRes, abs(delta)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(SanguSantriSpacing.default))
        Button(onClick = { onRetakeQuiz(uiState.packageId) }) {
            Text(text = stringResource(R.string.nahwu_quiz_result_retake_action))
        }
        OutlinedButton(onClick = { onViewHistory(uiState.packageId) }) {
            Text(text = stringResource(R.string.nahwu_quiz_result_view_history_action))
        }
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizResultScreenPreview() {
    SanguSantriTheme {
        NahwuQuizResultScreen(
            uiState =
                NahwuQuizResultUiState.Content(
                    packageId = "nahwu-dasar-fixture",
                    packageTitle = "[FIXTURE] Nahwu Dasar",
                    scorePercent = 83,
                    correctCount = 5,
                    totalCount = 6,
                    previousScorePercent = 67,
                ),
            onViewHistory = {},
            onRetakeQuiz = {},
        )
    }
}
