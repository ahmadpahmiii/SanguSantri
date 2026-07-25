package com.sangusantri.app.feature.guidedreader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.Approval
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.feature.guidedreader.components.GuidedStepContent
import com.sangusantri.app.feature.guidedreader.components.TasbihActions
import com.sangusantri.app.feature.reader.ReaderUiAction
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderOverflowMenu
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.settings.ProgressionModeControl
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet
import com.sangusantri.app.feature.reader.toApprovalDisplay

private val GuidedReaderMaxWidth = 640.dp

/** Bundles [GuidedReaderScreen]'s callbacks so the function stays under the parameter-count limit. */
data class GuidedReaderCallbacks(
    val onAction: (GuidedReaderUiAction) -> Unit,
    val onSettingsAction: (ReaderUiAction) -> Unit,
    val onSetProgressionMode: (GuidedProgressionMode) -> Unit,
    val onBack: () -> Unit,
)

@Composable
fun GuidedReaderRoute(
    amaliyahSlug: String,
    onBack: () -> Unit,
    onSwitchToFull: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuidedReaderViewModel =
        hiltViewModel<GuidedReaderViewModel, GuidedReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(amaliyahSlug) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val switchToFullReady by viewModel.switchToFullReady.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is GuidedReaderUiState.StepVisible && state.isCompleted) {
            onBack()
        }
    }

    LaunchedEffect(switchToFullReady) {
        if (switchToFullReady) onSwitchToFull()
    }

    GuidedReaderScreen(
        uiState = uiState,
        settings = settings,
        callbacks =
            GuidedReaderCallbacks(
                onAction = viewModel::onAction,
                onSettingsAction = viewModel::onSettingsAction,
                onSetProgressionMode = viewModel::setProgressionMode,
                onBack = onBack,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedReaderScreen(
    uiState: GuidedReaderUiState,
    settings: ReaderSettings,
    callbacks: GuidedReaderCallbacks,
    modifier: Modifier = Modifier,
) {
    val showSettings = rememberSaveable { mutableStateOf(false) }
    val showResetConfirm = rememberSaveable { mutableStateOf(false) }
    val showCompletionConfirm = rememberSaveable { mutableStateOf(false) }

    val title = (uiState as? GuidedReaderUiState.StepVisible)?.amaliyahTitleId ?: stringResource(R.string.app_name)

    Scaffold(
        modifier = modifier,
        topBar = {
            GuidedReaderTopBar(
                title = title,
                onBack = callbacks.onBack,
                onOpenSettings = { showSettings.value = true },
                overflow = {
                    if (uiState is GuidedReaderUiState.StepVisible) {
                        ReaderOverflowMenu(
                            switchModeLabel = stringResource(R.string.reader_switch_to_full_action),
                            onSwitchMode = { callbacks.onAction(GuidedReaderUiAction.SwitchToFull) },
                            approvalDisplay = uiState.approval.toApprovalDisplay(BuildConfig.DEBUG),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is GuidedReaderUiState.StepVisible) {
                GuidedReaderBottomBar(
                    state = uiState,
                    onPrevious = { callbacks.onAction(GuidedReaderUiAction.Previous) },
                    onContinue = {
                        if (uiState.isLastStep) {
                            showCompletionConfirm.value = true
                        } else {
                            callbacks.onAction(GuidedReaderUiAction.Continue)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        GuidedReaderContent(
            uiState = uiState,
            callbacks = callbacks,
            onRequestReset = { showResetConfirm.value = true },
            modifier = Modifier.padding(innerPadding),
        )
    }

    GuidedReaderOverlays(
        settings = settings,
        callbacks = callbacks,
        showSettings = showSettings,
        showResetConfirm = showResetConfirm,
        showCompletionConfirm = showCompletionConfirm,
    )
}

@Composable
private fun GuidedReaderContent(
    uiState: GuidedReaderUiState,
    callbacks: GuidedReaderCallbacks,
    onRequestReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        GuidedReaderUiState.Loading -> ReaderLoadingState(modifier = modifier)
        GuidedReaderUiState.ContentUnavailable -> ReaderContentUnavailableState(modifier = modifier)

        GuidedReaderUiState.RecoverableError ->
            ReaderRecoverableErrorState(
                onRetry = { callbacks.onAction(GuidedReaderUiAction.Retry) },
                modifier = modifier,
            )

        is GuidedReaderUiState.StepVisible ->
            GuidedReaderBody(
                state = uiState,
                actions =
                    TasbihActions(
                        onIncrement = { callbacks.onAction(GuidedReaderUiAction.IncrementCounter) },
                        onRequestReset = onRequestReset,
                    ),
                modifier = modifier,
            )
    }
}

@Composable
private fun GuidedReaderOverlays(
    settings: ReaderSettings,
    callbacks: GuidedReaderCallbacks,
    showSettings: MutableState<Boolean>,
    showResetConfirm: MutableState<Boolean>,
    showCompletionConfirm: MutableState<Boolean>,
) {
    if (showSettings.value) {
        ReaderSettingsSheet(
            settings = settings,
            onAction = callbacks.onSettingsAction,
            onDismiss = { showSettings.value = false },
            progressionModeControl =
                ProgressionModeControl(
                    mode = settings.guidedProgressionMode,
                    onChange = callbacks.onSetProgressionMode,
                ),
        )
    }

    if (showResetConfirm.value) {
        GuidedConfirmDialog(
            text =
                ConfirmDialogText(
                    title = stringResource(R.string.guided_counter_reset_dialog_title),
                    message = stringResource(R.string.guided_counter_reset_dialog_message),
                    confirmLabel = stringResource(R.string.guided_counter_reset_confirm_action),
                    cancelLabel = stringResource(R.string.guided_counter_reset_cancel_action),
                ),
            onDismiss = { showResetConfirm.value = false },
            onConfirm = {
                showResetConfirm.value = false
                callbacks.onAction(GuidedReaderUiAction.ResetCounter)
            },
        )
    }

    if (showCompletionConfirm.value) {
        GuidedConfirmDialog(
            text =
                ConfirmDialogText(
                    title = stringResource(R.string.guided_completion_dialog_title),
                    message = stringResource(R.string.guided_completion_dialog_message),
                    confirmLabel = stringResource(R.string.guided_completion_confirm_action),
                    cancelLabel = stringResource(R.string.guided_completion_cancel_action),
                ),
            onDismiss = { showCompletionConfirm.value = false },
            onConfirm = {
                showCompletionConfirm.value = false
                callbacks.onAction(GuidedReaderUiAction.ConfirmCompletion)
            },
        )
    }
}

/** Bundles [GuidedConfirmDialog]'s four text values so the function stays under the parameter-count limit. */
private data class ConfirmDialogText(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val cancelLabel: String,
)

@Composable
private fun GuidedConfirmDialog(
    text: ConfirmDialogText,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = text.title) },
        text = { Text(text = text.message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = text.confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = text.cancelLabel) } },
    )
}

@Composable
private fun GuidedReaderBody(
    state: GuidedReaderUiState.StepVisible,
    actions: TasbihActions,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val positionLabel =
            stringResource(R.string.guided_reader_position_label, state.stepIndex + 1, state.stepCount)
        Column(
            modifier =
                Modifier
                    .widthIn(max = GuidedReaderMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(SanguSantriSpacing.default),
        ) {
            Text(
                text = positionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .semantics {
                            contentDescription = positionLabel
                            liveRegion = LiveRegionMode.Polite
                        },
            )
            Spacer(Modifier.padding(top = SanguSantriSpacing.small))
            AnimatedContent(
                targetState = state.step,
                contentKey = { it.id },
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "guided-step-transition",
            ) { step ->
                GuidedStepContent(
                    step = step,
                    settings = state.settings,
                    currentCount = state.currentCount,
                    actions = actions,
                )
            }
        }
    }
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewCounterStep =
    AmaliyahStep(
        id = "step-2",
        versionId = "preview-version",
        position = 2,
        stepType = StepType.REPEATED_READING,
        titleId = null,
        titleAr = null,
        arabicText = "[FIXTURE-AR] سُبْحَانَ اللَّهِ",
        translationId = "[FIXTURE] Maha Suci Allah.",
        instructionId = null,
        instructionAr = null,
        repeatTarget = 33,
        quranSurahNumber = null,
        quranAyahStart = null,
        quranAyahEnd = null,
        audioGroupId = null,
    )

private val previewApproval =
    Approval(
        id = "preview-approval",
        approverName = "[FIXTURE] KH. Contoh Sesepuh",
        approverRole = "[FIXTURE]",
        institutionName = null,
        approvalDate = "2026-01-01",
        approvalScope = "[FIXTURE]",
        publicDocumentStorageKey = null,
        documentReferenceNumber = null,
        status = ApprovalStatus.APPROVED,
    )

private fun previewStepVisible(currentCount: Int = 12) =
    GuidedReaderUiState.StepVisible(
        amaliyahTitleId = "Tahlil",
        versionId = "preview-version",
        step = previewCounterStep,
        stepIndex = 4,
        stepCount = 22,
        currentCount = currentCount,
        settings = ReaderSettings(),
        isFirstStep = false,
        isLastStep = false,
        continueEnabled = currentCount >= 33,
        allRequiredCountersComplete = false,
        isCompleted = false,
        approval = previewApproval,
    )

private val previewCallbacks =
    GuidedReaderCallbacks(
        onAction = {},
        onSettingsAction = {},
        onSetProgressionMode = {},
        onBack = {},
    )

@PreviewLightDark
@Composable
private fun GuidedReaderScreenPreview() {
    SanguSantriTheme {
        GuidedReaderScreen(
            uiState = previewStepVisible(),
            settings = ReaderSettings(),
            callbacks = previewCallbacks,
        )
    }
}

@Preview(name = "Content unavailable")
@Composable
private fun GuidedReaderScreenUnavailablePreview() {
    SanguSantriTheme {
        GuidedReaderScreen(
            uiState = GuidedReaderUiState.ContentUnavailable,
            settings = ReaderSettings(),
            callbacks = previewCallbacks,
        )
    }
}
