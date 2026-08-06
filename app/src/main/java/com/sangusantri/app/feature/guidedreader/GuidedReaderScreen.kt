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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.guidedreader.components.GuidedStepContent
import com.sangusantri.app.feature.guidedreader.components.GuidedStepStatusRow
import com.sangusantri.app.feature.guidedreader.components.TasbihActions
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderOverflowActions
import com.sangusantri.app.feature.reader.components.ReaderOverflowMenu
import com.sangusantri.app.feature.reader.components.ReaderProgressHeader
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.components.ReaderSavedPositionStatus
import com.sangusantri.app.feature.reader.settings.ProgressionModeControl
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet

@Composable
fun GuidedReaderRoute(
    contentId: String,
    onBack: () -> Unit,
    onSwitchToFull: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuidedReaderViewModel =
        hiltViewModel<GuidedReaderViewModel, GuidedReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(contentId) },
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
    val title = (uiState as? GuidedReaderUiState.StepVisible)?.title ?: stringResource(R.string.app_name)

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

    GuidedReaderOverlays(settings = settings, callbacks = callbacks, overlays = overlays)
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
                            onOpenSettings = { overlays.showSettings.value = true },
                        ),
                    sourceName = uiState.sourceName,
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
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = SanguSantriDimensions.readerHorizontalPadding,
                        vertical = SanguSantriSpacing.default,
                    ),
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
    ContentStep(
        id = "step-2",
        contentId = "preview-content",
        position = 2,
        arabicText = "[FIXTURE-AR] سُبْحَانَ اللَّهِ",
        translation = "[FIXTURE] Maha Suci Allah.",
        repeatTarget = 33,
    )

private fun previewStepVisible(currentCount: Int = 12) =
    GuidedReaderUiState.StepVisible(
        title = "Tahlil",
        contentId = "preview-content",
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
