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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.sangusantri.app.feature.guidedreader.components.GuidedStepStatusRow
import com.sangusantri.app.feature.guidedreader.components.TasbihActions
import com.sangusantri.app.feature.reader.ReaderUiAction
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderOverflowActions
import com.sangusantri.app.feature.reader.components.ReaderOverflowMenu
import com.sangusantri.app.feature.reader.components.ReaderProgressHeader
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.components.ReaderSavedPositionStatus
import com.sangusantri.app.feature.reader.settings.ProgressionModeControl
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet
import com.sangusantri.app.feature.reader.toApprovalDisplay
import com.sangusantri.app.feature.reader.toc.ReaderTableOfContentsSheet
import com.sangusantri.app.feature.reader.toc.sectionContaining
import com.sangusantri.app.feature.reader.toc.toTocSections

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
    val overlays = rememberGuidedReaderOverlayVisibility()
    val showSavedPosition = rememberInitialSavedPositionFlag(uiState)
    val title = (uiState as? GuidedReaderUiState.StepVisible)?.amaliyahTitleId ?: stringResource(R.string.app_name)

    Scaffold(
        modifier = modifier,
        topBar = { GuidedReaderTopBarWithOverflow(title, uiState, callbacks, overlays) },
        bottomBar = { GuidedReaderBottomBarIfVisible(uiState, callbacks, overlays.showCompletionConfirm) },
    ) { innerPadding ->
        GuidedReaderContent(
            uiState = uiState,
            callbacks = callbacks,
            showSavedPosition = showSavedPosition,
            onRequestReset = { overlays.showResetConfirm.value = true },
            modifier = Modifier.padding(innerPadding),
        )
    }

    GuidedReaderOverlays(uiState = uiState, settings = settings, callbacks = callbacks, overlays = overlays)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuidedReaderTopBarWithOverflow(
    title: String,
    uiState: GuidedReaderUiState,
    callbacks: GuidedReaderCallbacks,
    overlays: GuidedReaderOverlayVisibility,
) {
    GuidedReaderTopBar(
        title = title,
        onBack = callbacks.onBack,
        overflow = {
            if (uiState is GuidedReaderUiState.StepVisible) {
                ReaderOverflowMenu(
                    switchModeLabel = stringResource(R.string.reader_switch_to_full_action),
                    actions =
                        ReaderOverflowActions(
                            onSwitchMode = { callbacks.onAction(GuidedReaderUiAction.SwitchToFull) },
                            onOpenTableOfContents = { overlays.showTableOfContents.value = true },
                            onOpenSettings = { overlays.showSettings.value = true },
                        ),
                    sourceName = uiState.sourceName,
                    approvalDisplay = uiState.approval.toApprovalDisplay(BuildConfig.DEBUG),
                )
            }
        },
    )
}

@Composable
private fun GuidedReaderBottomBarIfVisible(
    uiState: GuidedReaderUiState,
    callbacks: GuidedReaderCallbacks,
    showCompletionConfirm: MutableState<Boolean>,
) {
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
}

@Composable
private fun GuidedReaderContent(
    uiState: GuidedReaderUiState,
    callbacks: GuidedReaderCallbacks,
    showSavedPosition: Boolean,
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
                showSavedPosition = showSavedPosition,
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
    uiState: GuidedReaderUiState,
    settings: ReaderSettings,
    callbacks: GuidedReaderCallbacks,
    overlays: GuidedReaderOverlayVisibility,
) {
    if (overlays.showSettings.value) {
        ReaderSettingsSheet(
            settings = settings,
            onAction = callbacks.onSettingsAction,
            onDismiss = { overlays.showSettings.value = false },
            progressionModeControl =
                ProgressionModeControl(
                    mode = settings.guidedProgressionMode,
                    onChange = callbacks.onSetProgressionMode,
                ),
        )
    }

    if (overlays.showTableOfContents.value && uiState is GuidedReaderUiState.StepVisible) {
        val sections = uiState.allSteps.toTocSections()
        ReaderTableOfContentsSheet(
            sections = sections,
            currentSectionStepId = sections.sectionContaining(uiState.step.position)?.stepId,
            onSectionSelected = { stepId ->
                overlays.showTableOfContents.value = false
                callbacks.onAction(GuidedReaderUiAction.JumpToStep(stepId))
            },
            onDismiss = { overlays.showTableOfContents.value = false },
        )
    }

    if (overlays.showResetConfirm.value) {
        GuidedConfirmDialog(
            text =
                ConfirmDialogText(
                    title = stringResource(R.string.guided_counter_reset_dialog_title),
                    message = stringResource(R.string.guided_counter_reset_dialog_message),
                    confirmLabel = stringResource(R.string.guided_counter_reset_confirm_action),
                    cancelLabel = stringResource(R.string.guided_counter_reset_cancel_action),
                ),
            onDismiss = { overlays.showResetConfirm.value = false },
            onConfirm = {
                overlays.showResetConfirm.value = false
                callbacks.onAction(GuidedReaderUiAction.ResetCounter)
            },
        )
    }

    if (overlays.showCompletionConfirm.value) {
        GuidedConfirmDialog(
            text =
                ConfirmDialogText(
                    title = stringResource(R.string.guided_completion_dialog_title),
                    message = stringResource(R.string.guided_completion_dialog_message),
                    confirmLabel = stringResource(R.string.guided_completion_confirm_action),
                    cancelLabel = stringResource(R.string.guided_completion_cancel_action),
                ),
            onDismiss = { overlays.showCompletionConfirm.value = false },
            onConfirm = {
                overlays.showCompletionConfirm.value = false
                callbacks.onAction(GuidedReaderUiAction.ConfirmCompletion)
            },
        )
    }
}

@Composable
private fun GuidedReaderBody(
    state: GuidedReaderUiState.StepVisible,
    showSavedPosition: Boolean,
    actions: TasbihActions,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = GuidedReaderMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(SanguSantriSpacing.default),
        ) {
            ReaderProgressHeader(currentPosition = state.stepIndex + 1, totalSteps = state.stepCount)
            Spacer(Modifier.padding(top = SanguSantriSpacing.default))
            GuidedStepStatusRow(step = state.step)
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
            Spacer(Modifier.padding(top = SanguSantriSpacing.default))
            ReaderSavedPositionStatus(
                show = showSavedPosition,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
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
        titleId = "[FIXTURE] Tasbih",
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
        allSteps = listOf(previewCounterStep),
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
        sourceName = "NU Online — Bacaan Tahlil Singkat, Lengkap dengan Doa dan Terjemahannya",
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
