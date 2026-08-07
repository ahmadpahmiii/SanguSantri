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
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageStatus
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizContentUnavailableState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizEmptyPackageState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizOfflineBanner
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizProgressRow

@Composable
fun NahwuQuizPackageDetailRoute(
    packageId: String,
    onBack: () -> Unit,
    onStart: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizPackageDetailViewModel =
        hiltViewModel<NahwuQuizPackageDetailViewModel, NahwuQuizPackageDetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(packageId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizPackageDetailScreen(uiState = uiState, onBack = onBack, onStart = onStart, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizPackageDetailScreen(
    uiState: NahwuQuizPackageDetailUiState,
    onBack: () -> Unit,
    onStart: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nahwu_quiz_detail_title)) },
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
            NahwuQuizPackageDetailUiState.Loading ->
                NahwuQuizLoadingState(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize())

            NahwuQuizPackageDetailUiState.NotFound ->
                NahwuQuizContentUnavailableState(
                    onRetry = onBack,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )

            is NahwuQuizPackageDetailUiState.Content ->
                DetailContent(
                    summary = uiState.summary,
                    onStart = onStart,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun DetailContent(
    summary: NahwuQuizPackageSummary,
    onStart: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (summary.status == NahwuQuizPackageStatus.UNAVAILABLE) {
        NahwuQuizEmptyPackageState(modifier = modifier)
        return
    }

    Column(
        modifier = modifier.padding(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        NahwuQuizOfflineBanner()
        Text(text = summary.quizPackage.title, style = MaterialTheme.typography.headlineSmall)
        if (summary.quizPackage.description.isNotBlank()) {
            Text(text = summary.quizPackage.description, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text =
                stringResource(
                    R.string.nahwu_quiz_detail_question_count,
                    summary.quizPackage.questionCount,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val answeredCount = summary.answeredCount
        if (answeredCount != null) {
            NahwuQuizProgressRow(answeredCount = answeredCount, totalCount = summary.quizPackage.questionCount)
        }
        val actionLabelRes =
            if (summary.status == NahwuQuizPackageStatus.IN_PROGRESS) {
                R.string.nahwu_quiz_detail_resume_action
            } else {
                R.string.nahwu_quiz_detail_start_action
            }
        Button(onClick = { onStart(summary.quizPackage.id) }) {
            Text(text = stringResource(actionLabelRes))
        }
    }
}

// Development-only preview fixture — bracketed placeholder, never real quiz content.
private val previewSummary =
    NahwuQuizPackageSummary(
        quizPackage =
            NahwuQuizPackage(
                id = "nahwu-dasar-fixture",
                title = "[FIXTURE] Nahwu Dasar",
                description = "[FIXTURE] Paket contoh pengembangan — bukan konten produksi.",
                order = 1,
                isActive = true,
                questionCount = 6,
            ),
        status = NahwuQuizPackageStatus.IN_PROGRESS,
        answeredCount = 3,
    )

@PreviewLightDark
@Composable
private fun NahwuQuizPackageDetailScreenPreview() {
    SanguSantriTheme {
        NahwuQuizPackageDetailScreen(
            uiState = NahwuQuizPackageDetailUiState.Content(previewSummary),
            onBack = {},
            onStart = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizPackageDetailScreenEmptyPreview() {
    SanguSantriTheme {
        NahwuQuizPackageDetailScreen(
            uiState =
                NahwuQuizPackageDetailUiState.Content(
                    previewSummary.copy(
                        quizPackage = previewSummary.quizPackage.copy(questionCount = 0),
                        status = NahwuQuizPackageStatus.UNAVAILABLE,
                        answeredCount = null,
                    ),
                ),
            onBack = {},
            onStart = {},
        )
    }
}
