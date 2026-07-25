package com.sangusantri.app.feature.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.Approval
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderOverflowMenu
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.components.ReaderStepItem
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet

private val ReaderMaxWidth = 640.dp

@Composable
fun ReaderRoute(
    amaliyahSlug: String,
    onBack: () -> Unit,
    onSwitchToGuided: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel =
        hiltViewModel<ReaderViewModel, ReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(amaliyahSlug) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val switchToGuidedReady by viewModel.switchToGuidedReady.collectAsStateWithLifecycle()

    LaunchedEffect(switchToGuidedReady) {
        if (switchToGuidedReady) onSwitchToGuided()
    }

    ReaderScreen(
        uiState = uiState,
        settings = settings,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val title = (uiState as? ReaderUiState.ContentAvailable)?.amaliyahTitleId ?: stringResource(R.string.app_name)

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
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.reader_settings_content_description),
                        )
                    }
                    if (uiState is ReaderUiState.ContentAvailable) {
                        ReaderOverflowMenu(
                            switchModeLabel = stringResource(R.string.reader_switch_to_guided_action),
                            onSwitchMode = { onAction(ReaderUiAction.SwitchToGuided) },
                            approvalDisplay = uiState.approval.toApprovalDisplay(BuildConfig.DEBUG),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ReaderUiState.Loading -> ReaderLoadingState(modifier = Modifier.padding(innerPadding))
            ReaderUiState.ContentUnavailable -> ReaderContentUnavailableState(modifier = Modifier.padding(innerPadding))
            ReaderUiState.RecoverableError ->
                ReaderRecoverableErrorState(
                    onRetry = { onAction(ReaderUiAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )

            is ReaderUiState.ContentAvailable ->
                ReaderStepList(
                    contentState = uiState,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }

    if (showSettings) {
        ReaderSettingsSheet(
            settings = settings,
            onAction = onAction,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun ReaderStepList(
    contentState: ReaderUiState.ContentAvailable,
    onAction: (ReaderUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = contentState.initialItemIndex,
            initialFirstVisibleItemScrollOffset = contentState.initialItemOffset,
        )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> onAction(ReaderUiAction.ScrollPositionChanged(index, offset)) }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        onAction(
            ReaderUiAction.PersistPositionNow(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset),
        )
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .widthIn(max = ReaderMaxWidth)
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = SanguSantriSpacing.default,
                    vertical = SanguSantriSpacing.default,
                ),
        ) {
            items(items = contentState.steps, key = { it.id }) { step ->
                ReaderStepItem(step = step, settings = contentState.settings)
            }
        }
    }
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewSteps =
    listOf(
        AmaliyahStep(
            id = "step-1",
            versionId = "preview-version",
            position = 1,
            stepType = StepType.HEADING,
            titleId = "Pembukaan",
            titleAr = "[FIXTURE-AR] اَلْفَاتِحَة",
            arabicText = null,
            translationId = null,
            instructionId = null,
            instructionAr = null,
            repeatTarget = null,
            quranSurahNumber = null,
            quranAyahStart = null,
            quranAyahEnd = null,
            audioGroupId = null,
        ),
        AmaliyahStep(
            id = "step-2",
            versionId = "preview-version",
            position = 2,
            stepType = StepType.ARABIC_TEXT,
            titleId = null,
            titleAr = null,
            arabicText = "[FIXTURE-AR] بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            translationId = "[FIXTURE] Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
            instructionId = null,
            instructionAr = null,
            repeatTarget = null,
            quranSurahNumber = null,
            quranAyahStart = null,
            quranAyahEnd = null,
            audioGroupId = null,
        ),
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

private fun previewContentState(settings: ReaderSettings = ReaderSettings()) =
    ReaderUiState.ContentAvailable(
        amaliyahTitleId = "Tahlil",
        versionId = "preview-version",
        steps = previewSteps,
        settings = settings,
        initialItemIndex = 0,
        initialItemOffset = 0,
        approval = previewApproval,
    )

@PreviewLightDark
@Composable
private fun ReaderScreenContentPreview() {
    SanguSantriTheme {
        ReaderScreen(
            uiState = previewContentState(),
            settings = ReaderSettings(),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(name = "Content unavailable")
@Composable
private fun ReaderScreenUnavailablePreview() {
    SanguSantriTheme {
        ReaderScreen(
            uiState = ReaderUiState.ContentUnavailable,
            settings = ReaderSettings(),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(name = "Compact width", widthDp = 360, heightDp = 640)
@Composable
private fun ReaderScreenCompactPreview() {
    SanguSantriTheme {
        ReaderScreen(
            uiState = previewContentState(),
            settings = ReaderSettings(),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(name = "Expanded width", widthDp = 1024, heightDp = 700)
@Composable
private fun ReaderScreenExpandedPreview() {
    SanguSantriTheme {
        ReaderScreen(
            uiState = previewContentState(),
            settings = ReaderSettings(),
            onAction = {},
            onBack = {},
        )
    }
}
