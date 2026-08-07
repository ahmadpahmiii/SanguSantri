package com.sangusantri.app.feature.nahwuquiz

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sangusantri.app.domain.model.NahwuQuizOption
import com.sangusantri.app.domain.model.NahwuQuizOptionKey
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizAnswerOption
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizAnswerOptionState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizContentUnavailableState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizEmptyPackageState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState

@Composable
fun NahwuQuizSessionRoute(
    packageId: String,
    onBack: () -> Unit,
    onCompleted: (attemptId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizSessionViewModel =
        hiltViewModel<NahwuQuizSessionViewModel, NahwuQuizSessionViewModel.Factory>(
            creationCallback = { factory -> factory.create(packageId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState) {
        val completed = uiState as? NahwuQuizSessionUiState.Completed ?: return@LaunchedEffect
        onCompleted(completed.attemptId)
    }
    NahwuQuizSessionScreen(uiState = uiState, onBack = onBack, onAction = viewModel::onAction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizSessionScreen(
    uiState: NahwuQuizSessionUiState,
    onBack: () -> Unit,
    onAction: (NahwuQuizSessionUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is NahwuQuizSessionUiState.QuestionVisible) Text(text = uiState.packageTitle)
                },
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
            NahwuQuizSessionUiState.Loading ->
                NahwuQuizLoadingState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            NahwuQuizSessionUiState.ContentUnavailable ->
                NahwuQuizEmptyPackageState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            NahwuQuizSessionUiState.RecoverableError ->
                NahwuQuizContentUnavailableState(
                    onRetry = { onAction(NahwuQuizSessionUiAction.Retry) },
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )

            is NahwuQuizSessionUiState.Completed -> Unit // NahwuQuizSessionRoute navigates away.

            is NahwuQuizSessionUiState.QuestionVisible ->
                QuestionContent(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun QuestionContent(
    uiState: NahwuQuizSessionUiState.QuestionVisible,
    onAction: (NahwuQuizSessionUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        QuizProgressIndicator(
            questionIndex = uiState.questionIndex,
            questionCount = uiState.questionCount,
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            item(key = "stem") {
                Text(text = uiState.question.stem, style = MaterialTheme.typography.bodyLarge)
            }
            items(items = uiState.question.options, key = { it.key.name }) { option ->
                NahwuQuizAnswerOption(
                    option = option,
                    state = option.stateFor(uiState),
                    enabled = !uiState.isSubmitted,
                    onClick = { onAction(NahwuQuizSessionUiAction.SelectOption(option.key)) },
                )
            }
            if (uiState.isSubmitted) {
                item(key = "feedback") { FeedbackCaption(uiState) }
            }
        }
        BottomAction(uiState = uiState, onAction = onAction, modifier = Modifier.padding(SanguSantriSpacing.default))
    }
}

private fun NahwuQuizOption.stateFor(uiState: NahwuQuizSessionUiState.QuestionVisible): NahwuQuizAnswerOptionState =
    when {
        uiState.isSubmitted && key == uiState.correctOption -> NahwuQuizAnswerOptionState.CORRECT
        uiState.isSubmitted && key == uiState.selectedOption -> NahwuQuizAnswerOptionState.INCORRECT
        uiState.isSubmitted -> NahwuQuizAnswerOptionState.DEFAULT
        key == uiState.selectedOption -> NahwuQuizAnswerOptionState.SELECTED
        else -> NahwuQuizAnswerOptionState.DEFAULT
    }

@Composable
private fun QuizProgressIndicator(
    questionIndex: Int,
    questionCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { if (questionCount == 0) 0f else questionIndex.toFloat() / questionCount },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.nahwu_quiz_session_progress, questionIndex + 1, questionCount),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
        )
    }
}

@Composable
private fun FeedbackCaption(uiState: NahwuQuizSessionUiState.QuestionVisible) {
    Column {
        if (uiState.isCorrect == true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.nahwu_quiz_session_feedback_correct),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = SanguSantriSpacing.small),
                )
            }
        } else {
            Text(
                text = stringResource(R.string.nahwu_quiz_session_feedback_incorrect),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        uiState.question.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
            )
        }
    }
}

@Composable
private fun BottomAction(
    uiState: NahwuQuizSessionUiState.QuestionVisible,
    onAction: (NahwuQuizSessionUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelRes =
        when {
            !uiState.isSubmitted && uiState.selectedOption == null -> R.string.nahwu_quiz_session_action_continue
            !uiState.isSubmitted -> R.string.nahwu_quiz_session_action_submit
            uiState.isCorrect == true -> R.string.nahwu_quiz_session_action_next_question
            else -> R.string.nahwu_quiz_session_action_continue
        }
    val onClick = {
        if (uiState.isSubmitted) {
            onAction(
                NahwuQuizSessionUiAction.Continue,
            )
        } else {
            onAction(NahwuQuizSessionUiAction.Submit)
        }
    }
    Button(
        onClick = onClick,
        enabled = uiState.isSubmitted || uiState.canSubmit,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(labelRes))
    }
}

// Development-only preview fixture — bracketed placeholder, never real quiz content.
private val previewQuestion =
    NahwuQuizQuestion(
        id = "q1",
        packageId = "nahwu-dasar-fixture",
        order = 1,
        stem = "[FIXTURE] Pertanyaan contoh nomor 1 — bukan soal Nahwu produksi.",
        options =
            listOf(
                NahwuQuizOption(NahwuQuizOptionKey.A, "[FIXTURE] Pilihan A"),
                NahwuQuizOption(NahwuQuizOptionKey.B, "[FIXTURE] Pilihan B"),
                NahwuQuizOption(NahwuQuizOptionKey.C, "[FIXTURE] Pilihan C"),
                NahwuQuizOption(NahwuQuizOptionKey.D, "[FIXTURE] Pilihan D"),
            ),
        correctOption = NahwuQuizOptionKey.B,
        explanation = "[FIXTURE] Penjelasan contoh — konten produksi memerlukan tinjauan editorial.",
    )

@PreviewLightDark
@Composable
private fun NahwuQuizSessionScreenDefaultPreview() {
    SanguSantriTheme {
        NahwuQuizSessionScreen(
            uiState =
                NahwuQuizSessionUiState.QuestionVisible(
                    packageTitle = "[FIXTURE] Nahwu Dasar",
                    questionIndex = 0,
                    questionCount = 6,
                    question = previewQuestion,
                    selectedOption = null,
                    isSubmitted = false,
                    isCorrect = null,
                    correctOption = null,
                ),
            onBack = {},
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizSessionScreenCorrectPreview() {
    SanguSantriTheme {
        NahwuQuizSessionScreen(
            uiState =
                NahwuQuizSessionUiState.QuestionVisible(
                    packageTitle = "[FIXTURE] Nahwu Dasar",
                    questionIndex = 0,
                    questionCount = 6,
                    question = previewQuestion,
                    selectedOption = NahwuQuizOptionKey.B,
                    isSubmitted = true,
                    isCorrect = true,
                    correctOption = NahwuQuizOptionKey.B,
                ),
            onBack = {},
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizSessionScreenIncorrectPreview() {
    SanguSantriTheme {
        NahwuQuizSessionScreen(
            uiState =
                NahwuQuizSessionUiState.QuestionVisible(
                    packageTitle = "[FIXTURE] Nahwu Dasar",
                    questionIndex = 0,
                    questionCount = 6,
                    question = previewQuestion,
                    selectedOption = NahwuQuizOptionKey.A,
                    isSubmitted = true,
                    isCorrect = false,
                    correctOption = NahwuQuizOptionKey.B,
                ),
            onBack = {},
            onAction = {},
        )
    }
}
