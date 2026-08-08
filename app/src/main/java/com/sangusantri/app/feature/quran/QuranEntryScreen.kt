package com.sangusantri.app.feature.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * The Al-Qur'an Kemenag entry gate opened from Beranda (QUR-FR-001/002, §6.1). [onReady] is only
 * invoked once the hub can actually render from Room — the NavHost turns that into the real
 * navigation to the hub, same pattern as [com.sangusantri.app.feature.reader.ReaderEntryRoute].
 */
@Composable
fun QuranEntryRoute(
    onBack: () -> Unit,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is QuranEntryUiState.Ready) onReady()
    }

    QuranThemeBoundary {
        QuranEntryScreen(
            uiState = uiState,
            onRetry = viewModel::retry,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
fun QuranEntryScreen(
    uiState: QuranEntryUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(QuranBackground)) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(contentColor = QuranArabicText),
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(SanguSantriSpacing.small),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back_content_description),
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (uiState) {
                QuranEntryUiState.Checking, QuranEntryUiState.Ready ->
                    CircularProgressIndicator(color = QuranPrimary)

                is QuranEntryUiState.Preparing -> QuranPreparingState(uiState)

                QuranEntryUiState.PreparationFailed ->
                    QuranEntryMessage(
                        title = stringResource(R.string.quran_entry_failed_title),
                        description = stringResource(R.string.quran_entry_failed_description),
                        actionLabel = stringResource(R.string.quran_entry_retry_action),
                        onAction = onRetry,
                    )

                QuranEntryUiState.OfflineNoLocalData ->
                    QuranEntryMessage(
                        title = stringResource(R.string.quran_entry_offline_title),
                        description = stringResource(R.string.quran_entry_offline_description),
                        actionLabel = stringResource(R.string.quran_entry_retry_action),
                        onAction = onRetry,
                    )
            }
        }
    }
}

@Composable
private fun QuranPreparingState(state: QuranEntryUiState.Preparing) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier = Modifier.padding(SanguSantriSpacing.large),
    ) {
        Text(
            text = stringResource(R.string.quran_entry_preparing_title),
            style = MaterialTheme.typography.titleMedium,
            color = QuranArabicText,
            textAlign = TextAlign.Center,
        )
        if (state.total > 0) {
            LinearProgressIndicator(
                progress = { state.completed / state.total.toFloat() },
                color = QuranPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.quran_entry_preparing_progress, state.completed, state.total),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
            )
        } else {
            CircularProgressIndicator(color = QuranPrimary)
        }
        Text(
            text = stringResource(R.string.quran_entry_preparing_description),
            style = MaterialTheme.typography.bodySmall,
            color = QuranMutedText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuranEntryMessage(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier = Modifier.padding(SanguSantriSpacing.large),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = QuranArabicText,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary, contentColor = QuranOnPrimary),
        ) {
            Text(text = actionLabel)
        }
    }
}

@Preview(name = "Preparing")
@Composable
private fun QuranEntryPreparingPreview() {
    QuranThemeBoundary {
        QuranEntryScreen(uiState = QuranEntryUiState.Preparing(42, 114), onRetry = {}, onBack = {})
    }
}

@Preview(name = "Failed")
@Composable
private fun QuranEntryFailedPreview() {
    QuranThemeBoundary {
        QuranEntryScreen(uiState = QuranEntryUiState.PreparationFailed, onRetry = {}, onBack = {})
    }
}

@Preview(name = "Offline, no local data")
@Composable
private fun QuranEntryOfflinePreview() {
    QuranThemeBoundary {
        QuranEntryScreen(uiState = QuranEntryUiState.OfflineNoLocalData, onRetry = {}, onBack = {})
    }
}
