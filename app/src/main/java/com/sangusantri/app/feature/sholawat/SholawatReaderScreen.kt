package com.sangusantri.app.feature.sholawat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.sholawat.components.SholawatVerseBlock

@Composable
fun SholawatReaderRoute(
    contentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SholawatReaderViewModel =
        hiltViewModel<SholawatReaderViewModel, SholawatReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(contentId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SholawatReaderScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * Sholawat's own reading screen (0.0.8) — deliberately not [com.sangusantri.app.feature.reader]'s
 * Full/Guided Reader. Opens Arabic-only by default (large font, still scrolls) with one global
 * toggle to switch to the compact Arabic+translation layout — both are local, non-persisted
 * Compose state, matching the stateless-v1 decision (no reading position, no settings DataStore).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SholawatReaderScreen(
    uiState: SholawatReaderUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTranslation by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? SholawatReaderUiState.ContentAvailable)?.title.orEmpty()
                    Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
                actions = {
                    if (uiState is SholawatReaderUiState.ContentAvailable) {
                        IconButton(onClick = { showTranslation = !showTranslation }) {
                            Icon(
                                imageVector = if (showTranslation) Icons.Filled.Translate else Icons.Outlined.Translate,
                                contentDescription =
                                    stringResource(
                                        if (showTranslation) {
                                            R.string.sholawat_hide_translation_content_description
                                        } else {
                                            R.string.sholawat_show_translation_content_description
                                        },
                                    ),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            SholawatReaderUiState.Loading -> ReaderLoadingState(modifier = Modifier.padding(innerPadding))
            SholawatReaderUiState.Unavailable ->
                ReaderContentUnavailableState(
                    modifier = Modifier.padding(innerPadding),
                )

            SholawatReaderUiState.RecoverableError ->
                ReaderRecoverableErrorState(onRetry = onRetry, modifier = Modifier.padding(innerPadding))

            is SholawatReaderUiState.ContentAvailable ->
                SholawatReaderStepList(
                    steps = uiState.steps,
                    showTranslation = showTranslation,
                    largeArabicMode = !showTranslation,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun SholawatReaderStepList(
    steps: List<ContentStep>,
    showTranslation: Boolean,
    largeArabicMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth),
            contentPadding = PaddingValues(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
        ) {
            items(items = steps, key = { it.id }) { step ->
                SholawatVerseBlock(
                    step = step,
                    showTranslation = showTranslation,
                    largeArabicMode = largeArabicMode,
                )
            }
        }
    }
}

// Development-only preview fixture — never real sholawat text.
private val previewSteps =
    listOf(
        ContentStep(
            id = "fixture-1",
            contentId = "sholawat-fixture",
            position = 0,
            arabicText = "[FIXTURE] Teks Arab.",
            translation = "[FIXTURE] Terjemahan.",
            repeatTarget = 1,
        ),
    )

@PreviewLightDark
@Composable
private fun SholawatReaderScreenPreview() {
    SanguSantriTheme {
        SholawatReaderScreen(
            uiState = SholawatReaderUiState.ContentAvailable(title = "[FIXTURE] Sholawat", steps = previewSteps),
            onBack = {},
            onRetry = {},
        )
    }
}
