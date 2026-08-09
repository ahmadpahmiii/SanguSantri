package com.sangusantri.app.feature.nahwuquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizLoadingState
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizOfflineBanner
import com.sangusantri.app.feature.nahwuquiz.components.NahwuQuizPackageCard

@Composable
fun NahwuQuizPackagesRoute(
    onBack: () -> Unit,
    onPackageSelected: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NahwuQuizPackagesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NahwuQuizPackagesScreen(
        uiState = uiState,
        onBack = onBack,
        onPackageSelected = onPackageSelected,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NahwuQuizPackagesScreen(
    uiState: NahwuQuizPackagesUiState,
    onBack: () -> Unit,
    onPackageSelected: (packageId: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.nahwu_quiz_packages_title)) },
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
            NahwuQuizPackagesUiState.Loading ->
                NahwuQuizLoadingState(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                )

            NahwuQuizPackagesUiState.ContentUnavailable ->
                NahwuQuizContentUnavailableState(
                    onRetry = onRetry,
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                )

            is NahwuQuizPackagesUiState.Content ->
                PackagesList(
                    summaries = uiState.summaries,
                    onPackageSelected = onPackageSelected,
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                )
        }
    }
}

@Composable
private fun PackagesList(
    summaries: List<NahwuQuizPackageSummary>,
    onPackageSelected: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        item(key = "offline_banner") { NahwuQuizOfflineBanner() }
        items(items = summaries, key = { it.quizPackage.id }) { summary ->
            NahwuQuizPackageCard(summary = summary, onClick = onPackageSelected)
        }
    }
}

// Development-only preview fixture — bracketed placeholders, never real quiz content.
private val previewSummaries =
    listOf(
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "nahwu-dasar-fixture",
                    title = "[FIXTURE] Nahwu Dasar",
                    description = "[FIXTURE] Paket contoh pengembangan.",
                    order = 1,
                    isActive = true,
                    questionCount = 6,
                ),
            status = NahwuQuizPackageStatus.NEW,
            answeredCount = null,
        ),
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "nahwu-lanjutan-fixture",
                    title = "[FIXTURE] Nahwu Lanjutan",
                    description = "[FIXTURE] Paket contoh pengembangan — belum memiliki soal.",
                    order = 2,
                    isActive = true,
                    questionCount = 0,
                ),
            status = NahwuQuizPackageStatus.UNAVAILABLE,
            answeredCount = null,
        ),
    )

@PreviewLightDark
@Composable
private fun NahwuQuizPackagesScreenPreview() {
    SanguSantriTheme {
        NahwuQuizPackagesScreen(
            uiState = NahwuQuizPackagesUiState.Content(previewSummaries),
            onBack = {},
            onPackageSelected = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizPackagesScreenUnavailablePreview() {
    SanguSantriTheme {
        NahwuQuizPackagesScreen(
            uiState = NahwuQuizPackagesUiState.ContentUnavailable,
            onBack = {},
            onPackageSelected = {},
            onRetry = {},
        )
    }
}
