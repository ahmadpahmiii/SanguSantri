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
import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizResumeCard

@Composable
fun NahwuQuizLandingRoute(
    onBack: () -> Unit,
    onViewPackages: () -> Unit,
    onResumeAttempt: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizLandingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizLandingScreen(
        uiState = uiState,
        onBack = onBack,
        onViewPackages = onViewPackages,
        onResumeAttempt = onResumeAttempt,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizLandingScreen(
    uiState: NahwuQuizLandingUiState,
    onBack: () -> Unit,
    onViewPackages: () -> Unit,
    onResumeAttempt: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nahwu_quiz_landing_title)) },
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
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            Text(text = stringResource(R.string.nahwu_quiz_landing_intro), style = MaterialTheme.typography.bodyLarge)
            uiState.activeAttempt?.let { active -> ResumeSection(active, onResumeAttempt) }
            Button(onClick = onViewPackages) {
                Text(text = stringResource(R.string.nahwu_quiz_landing_view_packages_action))
            }
        }
    }
}

@Composable
private fun ResumeSection(
    active: NahwuQuizActiveAttempt,
    onResumeAttempt: (packageId: String) -> Unit,
) {
    NahwuQuizResumeCard(
        packageTitle = active.packageTitle,
        answeredCount = active.answeredCount,
        totalCount = active.totalCount,
        onResumeClick = { onResumeAttempt(active.packageId) },
    )
}

@PreviewLightDark
@Composable
private fun NahwuQuizLandingScreenPreview() {
    SanguSantriTheme {
        NahwuQuizLandingScreen(
            uiState = NahwuQuizLandingUiState(activeAttempt = null),
            onBack = {},
            onViewPackages = {},
            onResumeAttempt = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizLandingScreenWithResumePreview() {
    SanguSantriTheme {
        NahwuQuizLandingScreen(
            uiState =
                NahwuQuizLandingUiState(
                    activeAttempt =
                        NahwuQuizActiveAttempt(
                            attemptId = "attempt",
                            packageId = "nahwu-dasar-fixture",
                            packageTitle = "[FIXTURE] Nahwu Dasar",
                            answeredCount = 3,
                            totalCount = 6,
                        ),
                ),
            onBack = {},
            onViewPackages = {},
            onResumeAttempt = {},
        )
    }
}
