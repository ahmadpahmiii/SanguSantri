package com.sangusantri.app.feature.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranEntryProgressTrackColor
import com.sangusantri.app.core.designsystem.theme.QuranError
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
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
    @Suppress("UnusedParameter") onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Approved local reference (design-export/quran/05a/05b/06a/06b) renders a `.top.plain` header
    // — title/subtitle only, no back icon, same pattern as the hub. System/predictive back still
    // pops this destination via the NavHost's own back stack, independent of this callback; kept
    // on the signature to mirror QuranEntryRoute/QuranHubActions rather than break the call site.
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(QuranBackground),
    ) {
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.quran_hub_title),
                style = MaterialTheme.typography.titleLarge,
                color = QuranArabicText,
            )
            Text(
                text = stringResource(R.string.quran_hub_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                QuranEntryUiState.Checking, QuranEntryUiState.Ready -> QuranCheckingState()

                is QuranEntryUiState.Preparing -> QuranPreparingState(uiState)

                is QuranEntryUiState.PreparationFailed ->
                    QuranEntryMessage(
                        title = stringResource(R.string.quran_entry_failed_title),
                        description = stringResource(R.string.quran_entry_failed_description),
                        actionLabel = stringResource(R.string.quran_entry_retry_action),
                        icon = Icons.Outlined.ErrorOutline,
                        onAction = onRetry,
                        detail = stringResource(R.string.quran_entry_failed_detail_label, uiState.reason),
                    )

                QuranEntryUiState.OfflineNoLocalData ->
                    QuranEntryMessage(
                        title = stringResource(R.string.quran_entry_offline_title),
                        description = stringResource(R.string.quran_entry_offline_description),
                        actionLabel = stringResource(R.string.quran_entry_retry_action),
                        icon = Icons.Outlined.CloudOff,
                        onAction = onRetry,
                    )
            }
        }
    }
}

@Composable
private fun QuranCheckingState() {
    QuranEntryStateLayout(
        title = stringResource(R.string.quran_entry_checking_title),
        description = stringResource(R.string.quran_entry_checking_description),
        stateVisual = { QuranEntryStateVisual(showProgress = true) },
    )
}

@Composable
private fun QuranPreparingState(state: QuranEntryUiState.Preparing) {
    QuranEntryStateLayout(
        title = stringResource(R.string.quran_entry_preparing_title),
        description = stringResource(R.string.quran_entry_preparing_description),
        stateVisual = { QuranEntryStateVisual(imageVector = Icons.Outlined.CloudDownload) },
        supportingContent = {
            if (state.total > 0) {
                LinearProgressIndicator(
                    progress = { state.completed / state.total.toFloat() },
                    color = QuranPrimary,
                    trackColor = QuranEntryProgressTrackColor,
                    modifier =
                        Modifier
                            .widthIn(max = SanguSantriDimensions.quranEntryProgressWidth)
                            .fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.quran_entry_preparing_progress, state.completed, state.total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranMutedText,
                )
            } else {
                CircularProgressIndicator(color = QuranPrimary)
            }
        },
    )
}

@Suppress("LongParameterList")
@Composable
private fun QuranEntryMessage(
    title: String,
    description: String,
    actionLabel: String,
    icon: ImageVector,
    onAction: () -> Unit,
    detail: String? = null,
) {
    QuranEntryStateLayout(
        title = title,
        description = description,
        // design-export/quran/06a/06b `.state-mark.error{color:var(--error)}` — both failure
        // branches use the error tint, unlike checking/preparing which stay QuranPrimary.
        stateVisual = { QuranEntryStateVisual(imageVector = icon, tint = QuranError) },
        supportingContent = {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary, contentColor = QuranOnPrimary),
                modifier =
                    Modifier
                        .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                        .widthIn(min = SanguSantriDimensions.quranStateActionButtonMinWidth),
            ) {
                Text(text = actionLabel)
            }
            // Raw, non-sensitive failure detail (never a credential, header, body, or Arabic/
            // translation content — see QuranSyncManager's ioReason/reason strings) so a real user
            // hitting a production failure can read and copy exactly what went wrong when
            // reporting it, instead of only a generic message.
            /* if (!detail.isNullOrBlank()) {
                 SelectionContainer {
                     Text(
                         text = detail,
                         style = MaterialTheme.typography.labelSmall,
                         color = QuranMutedText,
                         textAlign = TextAlign.Center,
                     )
                 }
             }*/
        },
    )
}

@Composable
private fun QuranEntryStateLayout(
    title: String,
    description: String,
    stateVisual: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier =
            Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .padding(SanguSantriSpacing.large),
    ) {
        stateVisual()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = QuranArabicText,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
            textAlign = TextAlign.Center,
        )
        supportingContent?.invoke()
    }
}

@Composable
private fun QuranEntryStateVisual(
    imageVector: ImageVector? = null,
    showProgress: Boolean = false,
    tint: Color = QuranPrimary,
) {
    Surface(
        color = QuranSurface,
        border = BorderStroke(1.dp, QuranOutline),
        shape = RoundedCornerShape(SanguSantriDimensions.quranStateMarkCornerRadius),
        modifier = Modifier.size(SanguSantriDimensions.quranStateMarkSize),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (showProgress) {
                CircularProgressIndicator(color = QuranPrimary, modifier = Modifier.size(32.dp))
            } else if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(34.dp),
                )
            }
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
        QuranEntryScreen(
            uiState = QuranEntryUiState.PreparationFailed(reason = "surah list network error: Unable to resolve host"),
            onRetry = {},
            onBack = {},
        )
    }
}

@Preview(name = "Offline, no local data")
@Composable
private fun QuranEntryOfflinePreview() {
    QuranThemeBoundary {
        QuranEntryScreen(uiState = QuranEntryUiState.OfflineNoLocalData, onRetry = {}, onBack = {})
    }
}
