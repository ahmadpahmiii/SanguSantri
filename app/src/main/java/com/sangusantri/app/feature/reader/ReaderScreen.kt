@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ThemeToggleButton
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.hasGuidedMode
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderModeToggle
import com.sangusantri.app.feature.reader.components.ReaderOverflowMenu
import com.sangusantri.app.feature.reader.components.ReaderProgressHeader
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.components.ReaderSavedPositionStatus
import com.sangusantri.app.feature.reader.components.ReaderStepItem
import com.sangusantri.app.feature.reader.components.ReaderTocSheet
import com.sangusantri.app.feature.reader.components.TocSectionItem
import com.sangusantri.app.feature.reader.components.rememberInitialSavedPositionFlag
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet
import kotlinx.coroutines.launch

@Composable
fun ReaderRoute(
    contentId: String,
    onBack: () -> Unit,
    onSwitchToGuided: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel =
        hiltViewModel<ReaderViewModel, ReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(contentId) },
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
    var showToc by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ReaderHeader(
                uiState = uiState,
                onBack = onBack,
                onAction = onAction,
                onOpenSettings = { showSettings = true },
                onOpenToc = { showToc = true },
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
                    showToc = showToc,
                    onDismissToc = { showToc = false },
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

/**
 * Top bar plus the pinned Bacaan Lengkap ⇄ Panduan toggle, drawn on one `surface` slab so the two
 * read as a single header (the Scaffold body sits on `background`). The toggle is absent for
 * content that has no Panduan mode at all (see [ReaderUiState.ContentAvailable.hasGuidedMode]).
 */
@Composable
private fun ReaderHeader(
    uiState: ReaderUiState,
    onBack: () -> Unit,
    onAction: (ReaderUiAction) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenToc: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            ReaderTopBar(
                uiState = uiState,
                onBack = onBack,
                onAction = onAction,
                onOpenSettings = onOpenSettings,
                onOpenToc = onOpenToc,
            )
            if ((uiState as? ReaderUiState.ContentAvailable)?.hasGuidedMode == true) {
                ReaderModeToggle(
                    current = ReaderMode.FULL,
                    onSelect = { onAction(ReaderUiAction.SwitchToGuided) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    uiState: ReaderUiState,
    onBack: () -> Unit,
    onAction: (ReaderUiAction) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenToc: () -> Unit,
) {
    val contentState = uiState as? ReaderUiState.ContentAvailable
    val title = contentState?.title ?: stringResource(R.string.app_name)
    TopAppBar(
        title = {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                contentState?.let {
                    Text(
                        text = stringResource(R.string.reader_step_count_subtitle, it.steps.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        expandedHeight = SanguSantriDimensions.compactTopAppBarHeight,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            ThemeToggleButton(onSelect = { onAction(ReaderUiAction.SetThemeMode(it)) })
            if (contentState != null) {
                ReaderOverflowMenu(
                    onOpenSettings = onOpenSettings,
                    onOpenToc = onOpenToc,
                    sourceName = contentState.sourceName,
                )
            }
        },
    )
}

@Composable
private fun ReaderStepList(
    contentState: ReaderUiState.ContentAvailable,
    showToc: Boolean,
    onDismissToc: () -> Unit,
    onAction: (ReaderUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = contentState.initialItemIndex,
            initialFirstVisibleItemScrollOffset = contentState.initialItemOffset,
        )
    val showSavedPosition = rememberInitialSavedPositionFlag(contentState.initialItemIndex)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) -> onAction(ReaderUiAction.ScrollPositionChanged(index, offset)) }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        onAction(
            ReaderUiAction.PersistPositionNow(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset),
        )
    }

    val currentItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, contentState.steps.lastIndex) }
    }

    val sections =
        remember(contentState.steps) {
            contentState.steps.mapIndexed { index, step ->
                val sampleText = step.arabicText.ifBlank { step.translation }
                TocSectionItem(
                    stepIndex = index,
                    title = if (sampleText.length > 35) sampleText.take(35) + "…" else sampleText,
                )
            }
        }

    ReaderStepListContent(
        contentState = contentState,
        renderState = ReaderStepListRenderState(listState, currentItemIndex, showSavedPosition),
        onAction = onAction,
        modifier = modifier,
    )

    if (showToc) {
        ReaderTocSheet(
            sections = sections,
            currentStepIndex = currentItemIndex,
            onSelectSection = { targetStepIndex ->
                coroutineScope.launch {
                    listState.animateScrollToItem(targetStepIndex)
                }
            },
            onDismiss = onDismissToc,
        )
    }
}

/** Bundles the composition-local rendering state [ReaderStepListContent] needs, under the parameter-count limit. */
private data class ReaderStepListRenderState(
    val listState: LazyListState,
    val currentItemIndex: Int,
    val showSavedPosition: Boolean,
)

@Composable
private fun ReaderStepListContent(
    contentState: ReaderUiState.ContentAvailable,
    renderState: ReaderStepListRenderState,
    onAction: (ReaderUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReaderProgressHeader(
                    currentPosition = renderState.currentItemIndex + 1,
                    totalSteps = contentState.steps.size,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = SanguSantriDimensions.readerHorizontalPadding,
                                vertical = SanguSantriSpacing.small,
                            ),
                )
                LazyColumn(
                    state = renderState.listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            horizontal = SanguSantriDimensions.readerHorizontalPadding,
                            vertical = SanguSantriSpacing.default,
                        ),
                ) {
                    items(items = contentState.steps, key = { it.id }) { step ->
                        ReaderStepItem(
                            step = step,
                            settings = contentState.settings,
                            onOpenGuidedAtStep = { stepId -> onAction(ReaderUiAction.SwitchToGuidedAtStep(stepId)) },
                        )
                    }
                }
            }
            ReaderSavedPositionStatus(
                show = renderState.showSavedPosition,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = SanguSantriSpacing.default),
            )
        }
    }
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewSteps =
    listOf(
        ContentStep(
            id = "step-1",
            contentId = "preview-content",
            position = 1,
            arabicText = "[FIXTURE-AR] بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            translation = "[FIXTURE] Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
            repeatTarget = 1,
        ),
        ContentStep(
            id = "step-2",
            contentId = "preview-content",
            position = 2,
            arabicText = "[FIXTURE-AR] سُبْحَانَ اللَّهِ",
            translation = "[FIXTURE] Maha Suci Allah.",
            repeatTarget = 33,
        ),
    )

private fun previewContentState(
    settings: ReaderSettings = ReaderSettings(),
    steps: List<ContentStep> = previewSteps,
) = ReaderUiState.ContentAvailable(
    title = "Tahlil",
    contentId = "preview-content",
    steps = steps,
    settings = settings,
    initialItemIndex = 0,
    initialItemOffset = 0,
    sourceName = "NU Online — Bacaan Tahlil Singkat, Lengkap dengan Doa dan Terjemahannya",
    hasGuidedMode = steps.hasGuidedMode(),
)

/** Sholawat-style content: nothing counted, so no Panduan pill and no overflow mode switch. */
private val previewUncountedSteps = previewSteps.map { it.copy(repeatTarget = null) }

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

@PreviewLightDark
@Composable
private fun ReaderScreenNoGuidedModePreview() {
    SanguSantriTheme {
        ReaderScreen(
            uiState = previewContentState(steps = previewUncountedSteps),
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
