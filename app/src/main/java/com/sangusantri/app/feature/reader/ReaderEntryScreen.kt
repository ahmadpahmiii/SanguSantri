package com.sangusantri.app.feature.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState

/**
 * The reading-mode gate opened from Serambi (PRD 8.2): resolves straight through to a remembered
 * mode, otherwise shows the [Bacaan Lengkap]/[Panduan] choice once. [onModeResolved] is only
 * invoked when a mode is settled (remembered or just chosen) — [NavHost][com.sangusantri.app.navigation.SanguSantriNavHost]
 * owns turning that into the actual navigation.
 */
@Composable
fun ReaderEntryRoute(
    contentId: String,
    onBack: () -> Unit,
    onModeResolved: (ReaderMode) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderEntryViewModel =
        hiltViewModel<ReaderEntryViewModel, ReaderEntryViewModel.Factory>(
            creationCallback = { factory -> factory.create(contentId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        (uiState as? ReaderEntryUiState.Resolved)?.let { onModeResolved(it.mode) }
    }

    ReaderEntryScreen(
        uiState = uiState,
        onSelectMode = viewModel::selectMode,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderEntryScreen(
    uiState: ReaderEntryUiState,
    onSelectMode: (ReaderMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (uiState as? ReaderEntryUiState.ModeChooser)?.title.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
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
            ReaderEntryUiState.Loading, is ReaderEntryUiState.Resolved ->
                ReaderLoadingState(modifier = Modifier.padding(innerPadding))

            ReaderEntryUiState.ContentUnavailable ->
                ReaderContentUnavailableState(modifier = Modifier.padding(innerPadding))

            is ReaderEntryUiState.ModeChooser ->
                ReaderModeChooser(onSelectMode = onSelectMode, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun ReaderModeChooser(
    onSelectMode: (ReaderMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        Text(
            text = stringResource(R.string.reader_mode_chooser_title),
            style = MaterialTheme.typography.titleLarge,
        )
        ReaderModeOption(
            title = stringResource(R.string.reader_mode_full_title),
            description = stringResource(R.string.reader_mode_full_description),
            onClick = { onSelectMode(ReaderMode.FULL) },
        )
        ReaderModeOption(
            title = stringResource(R.string.reader_mode_guided_title),
            description = stringResource(R.string.reader_mode_guided_description),
            onClick = { onSelectMode(ReaderMode.GUIDED) },
        )
    }
}

@Composable
private fun ReaderModeOption(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = SanguSantriShapes.medium,
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ReaderEntryScreenModeChooserPreview() {
    SanguSantriTheme {
        ReaderEntryScreen(
            uiState = ReaderEntryUiState.ModeChooser(title = "Tahlil"),
            onSelectMode = {},
            onBack = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ReaderEntryScreenUnavailablePreview() {
    SanguSantriTheme {
        ReaderEntryScreen(
            uiState = ReaderEntryUiState.ContentUnavailable,
            onSelectMode = {},
            onBack = {},
        )
    }
}
