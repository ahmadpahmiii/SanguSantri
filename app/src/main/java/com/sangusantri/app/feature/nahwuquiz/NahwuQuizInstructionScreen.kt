package com.sangusantri.app.feature.nahwuquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizContentUnavailableState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState

@Composable
fun NahwuQuizInstructionRoute(
    packageId: String,
    onBack: () -> Unit,
    onStartQuiz: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizInstructionViewModel =
        hiltViewModel<NahwuQuizInstructionViewModel, NahwuQuizInstructionViewModel.Factory>(
            creationCallback = { factory -> factory.create(packageId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizInstructionScreen(
        uiState = uiState,
        onBack = onBack,
        onStartQuiz = { onStartQuiz(packageId) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizInstructionScreen(
    uiState: NahwuQuizInstructionUiState,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nahwu_quiz_instruction_title)) },
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
            NahwuQuizInstructionUiState.Loading ->
                NahwuQuizLoadingState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            NahwuQuizInstructionUiState.NotFound ->
                NahwuQuizContentUnavailableState(
                    onRetry = onBack,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )

            is NahwuQuizInstructionUiState.Content ->
                InstructionContent(
                    uiState = uiState,
                    onStartQuiz = onStartQuiz,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun InstructionContent(
    uiState: NahwuQuizInstructionUiState.Content,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        Text(text = uiState.packageTitle, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.nahwu_quiz_instruction_question_count, uiState.questionCount),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = stringResource(R.string.nahwu_quiz_instruction_format), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.nahwu_quiz_instruction_autosave_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onStartQuiz) {
            Text(text = stringResource(R.string.nahwu_quiz_instruction_start_action))
        }
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizInstructionScreenPreview() {
    SanguSantriTheme {
        NahwuQuizInstructionScreen(
            uiState = NahwuQuizInstructionUiState.Content(packageTitle = "[FIXTURE] Nahwu Dasar", questionCount = 6),
            onBack = {},
            onStartQuiz = {},
        )
    }
}
