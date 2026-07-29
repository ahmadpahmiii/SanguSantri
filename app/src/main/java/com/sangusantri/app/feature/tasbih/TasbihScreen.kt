package com.sangusantri.app.feature.tasbih

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ConfirmationDialog
import com.sangusantri.app.core.designsystem.component.ConfirmationDialogText
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.feature.tasbih.components.CustomTasbihTargetDialog
import com.sangusantri.app.feature.tasbih.components.TasbihAutosaveCaption
import com.sangusantri.app.feature.tasbih.components.TasbihCounter
import com.sangusantri.app.feature.tasbih.components.TasbihCounterTone
import com.sangusantri.app.feature.tasbih.components.TasbihRestoredIndicatorRow
import com.sangusantri.app.feature.tasbih.components.TasbihSecondaryActions
import com.sangusantri.app.feature.tasbih.components.TasbihSessionNameField
import com.sangusantri.app.feature.tasbih.components.TasbihTargetHeaderLabel
import com.sangusantri.app.feature.tasbih.components.TasbihTargetSelector

@Composable
fun TasbihRoute(
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasbihViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TasbihScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onHistoryClick = onHistoryClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    uiState: TasbihUiState,
    onAction: (TasbihUiAction) -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeDialog by rememberSaveable { mutableStateOf(TasbihDialog.NONE) }
    val currentCount = (uiState as? TasbihUiState.Active)?.currentCount ?: 0

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tasbih_title)) },
                actions = {
                    if (currentCount > 0) {
                        IconButton(onClick = { activeDialog = TasbihDialog.RESET_CONFIRMATION }) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = stringResource(R.string.tasbih_reset_action),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        TasbihScreenContent(
            uiState = uiState,
            onAction = onAction,
            onHistoryClick = onHistoryClick,
            onRequestDialog = { dialog -> activeDialog = dialog },
            modifier = Modifier.padding(innerPadding),
        )
    }

    TasbihScreenDialogs(
        activeDialog = activeDialog,
        currentCount = currentCount,
        onAction = onAction,
        onDismiss = { activeDialog = TasbihDialog.NONE },
    )
}

@Composable
private fun TasbihScreenContent(
    uiState: TasbihUiState,
    onAction: (TasbihUiAction) -> Unit,
    onHistoryClick: () -> Unit,
    onRequestDialog: (TasbihDialog) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .align(Alignment.TopCenter)
                    .padding(SanguSantriSpacing.default),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            when (uiState) {
                TasbihUiState.NoSession ->
                    TasbihNoSessionContent(
                        onAction = onAction,
                        onHistoryClick = onHistoryClick,
                        onCustomTargetRequested = { onRequestDialog(TasbihDialog.CUSTOM_TARGET) },
                    )

                is TasbihUiState.Active ->
                    TasbihActiveContent(
                        state = uiState,
                        onAction = onAction,
                        onHistoryClick = onHistoryClick,
                        onCustomTargetRequested = { onRequestDialog(TasbihDialog.CUSTOM_TARGET) },
                        onResetRequested = { onRequestDialog(TasbihDialog.RESET_CONFIRMATION) },
                    )
            }
        }
    }
}

@Composable
private fun TasbihScreenDialogs(
    activeDialog: TasbihDialog,
    currentCount: Int,
    onAction: (TasbihUiAction) -> Unit,
    onDismiss: () -> Unit,
) {
    when (activeDialog) {
        TasbihDialog.NONE -> Unit

        TasbihDialog.CUSTOM_TARGET ->
            CustomTasbihTargetDialog(
                onDismiss = onDismiss,
                onConfirm = { value ->
                    onAction(TasbihUiAction.SetCustomTarget(value))
                    onDismiss()
                },
            )

        TasbihDialog.RESET_CONFIRMATION ->
            ConfirmationDialog(
                text =
                    ConfirmationDialogText(
                        title = stringResource(R.string.tasbih_reset_dialog_title),
                        message = stringResource(R.string.tasbih_reset_dialog_message, currentCount),
                        confirmLabel = stringResource(R.string.tasbih_reset_dialog_confirm_action),
                        cancelLabel = stringResource(R.string.tasbih_reset_dialog_cancel_action),
                    ),
                isDestructive = true,
                onConfirm = {
                    onAction(TasbihUiAction.ResetSession)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
    }
}

@Composable
private fun TasbihNoSessionContent(
    onAction: (TasbihUiAction) -> Unit,
    onHistoryClick: () -> Unit,
    onCustomTargetRequested: () -> Unit,
) {
    TasbihTargetHeaderLabel()
    Text(
        text = stringResource(R.string.tasbih_target_choose_prompt),
        style = MaterialTheme.typography.titleLarge,
    )
    TasbihCounter(
        count = 0,
        tone = TasbihCounterTone.NEUTRAL,
        stateDescription = stringResource(R.string.tasbih_counter_state_no_target, 0),
        onTap = { onAction(TasbihUiAction.IncrementCounter) },
    )
    Text(text = stringResource(R.string.tasbih_choose_target_label), style = MaterialTheme.typography.titleMedium)
    TasbihTargetSelector(
        selectedPreset = null,
        onPresetSelected = { onAction(TasbihUiAction.SelectPreset(it)) },
        onCustomRequested = onCustomTargetRequested,
    )
    TasbihAutosaveCaption()
    TasbihSecondaryActions(showReset = false, onResetClick = {}, onHistoryClick = onHistoryClick)
}

@Composable
private fun TasbihActiveContent(
    state: TasbihUiState.Active,
    onAction: (TasbihUiAction) -> Unit,
    onHistoryClick: () -> Unit,
    onCustomTargetRequested: () -> Unit,
    onResetRequested: () -> Unit,
) {
    TasbihSessionNameField(
        sessionName = state.sessionName,
        onRename = { onAction(TasbihUiAction.RenameSession(it)) },
    )
    if (state.isRestored) {
        TasbihRestoredIndicatorRow()
    }
    TasbihTargetHeaderLabel()
    TasbihTargetHeaderValue(state)

    val tone = if (state.isTargetReached) TasbihCounterTone.TARGET_REACHED else TasbihCounterTone.COUNTING
    val targetShortText = state.targetValue?.toString() ?: stringResource(R.string.tasbih_target_unlimited_short)
    val statusText =
        if (state.isTargetReached) {
            stringResource(R.string.tasbih_status_target_reached)
        } else {
            stringResource(R.string.tasbih_status_counting)
        }
    TasbihCounter(
        count = state.currentCount,
        tone = tone,
        stateDescription =
            stringResource(
                R.string.tasbih_counter_state_description,
                state.currentCount,
                targetShortText,
                statusText,
            ),
        onTap = { onAction(TasbihUiAction.IncrementCounter) },
    )

    Text(text = stringResource(R.string.tasbih_choose_target_label), style = MaterialTheme.typography.titleMedium)
    TasbihTargetSelector(
        selectedPreset = state.targetPreset,
        onPresetSelected = { onAction(TasbihUiAction.SelectPreset(it)) },
        onCustomRequested = onCustomTargetRequested,
    )
    TasbihAutosaveCaption()
    TasbihSecondaryActions(
        showReset = state.currentCount > 0,
        onResetClick = onResetRequested,
        onHistoryClick = onHistoryClick,
    )
}

@Composable
private fun TasbihTargetHeaderValue(state: TasbihUiState.Active) {
    val targetValueText =
        if (state.targetPreset == TasbihTargetPreset.UNLIMITED) {
            stringResource(R.string.tasbih_target_unlimited)
        } else {
            stringResource(R.string.tasbih_target_value, state.targetValue ?: 0)
        }
    Text(
        text = targetValueText,
        style = MaterialTheme.typography.titleLarge,
        color = if (state.isTargetReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
}

@PreviewLightDark
@Composable
private fun TasbihScreenActivePreview() {
    SanguSantriTheme {
        TasbihScreen(
            uiState =
                TasbihUiState.Active(
                    currentCount = 12,
                    targetValue = 33,
                    targetPreset = TasbihTargetPreset.THIRTY_THREE,
                    sessionName = null,
                    isTargetReached = false,
                    isRestored = false,
                ),
            onAction = {},
            onHistoryClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TasbihScreenTargetReachedPreview() {
    SanguSantriTheme {
        TasbihScreen(
            uiState =
                TasbihUiState.Active(
                    currentCount = 33,
                    targetValue = 33,
                    targetPreset = TasbihTargetPreset.THIRTY_THREE,
                    sessionName = "Tahlil malam Jumat",
                    isTargetReached = true,
                    isRestored = false,
                ),
            onAction = {},
            onHistoryClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TasbihScreenNoSessionPreview() {
    SanguSantriTheme {
        TasbihScreen(
            uiState = TasbihUiState.NoSession,
            onAction = {},
            onHistoryClick = {},
        )
    }
}
