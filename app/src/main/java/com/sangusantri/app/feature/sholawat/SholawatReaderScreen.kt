package com.sangusantri.app.feature.sholawat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.outlined.FormatSize
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ContentLayout
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.reader.ReaderUiAction
import com.sangusantri.app.feature.reader.components.ReaderContentUnavailableState
import com.sangusantri.app.feature.reader.components.ReaderLoadingState
import com.sangusantri.app.feature.reader.components.ReaderRecoverableErrorState
import com.sangusantri.app.feature.reader.settings.ReaderSettingsExtras
import com.sangusantri.app.feature.reader.settings.ReaderSettingsSheet
import com.sangusantri.app.feature.reader.settings.SholawatLayoutControl
import com.sangusantri.app.feature.sholawat.components.SholawatBaytRow
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
        callbacks =
            SholawatReaderCallbacks(
                onSettingsAction = viewModel::onSettingsAction,
                onTwoColumnChange = viewModel::setTwoColumn,
                onBaitGapChange = viewModel::setBaitGap,
            ),
        modifier = modifier,
    )
}

/** Bundles the reader's settings callbacks so the screen stays under the parameter-count limit. */
data class SholawatReaderCallbacks(
    val onSettingsAction: (ReaderUiAction) -> Unit = {},
    val onTwoColumnChange: (Boolean) -> Unit = {},
    val onBaitGapChange: (Boolean) -> Unit = {},
)

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
    callbacks: SholawatReaderCallbacks = SholawatReaderCallbacks(),
    modifier: Modifier = Modifier,
) {
    val content = uiState as? SholawatReaderUiState.ContentAvailable
    val showTranslation = content?.settings?.showTranslation == true
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            SholawatReaderTopBar(
                title = content?.title.orEmpty(),
                hasContent = content != null,
                showTranslation = showTranslation,
                onBack = onBack,
                actions =
                    SholawatTopBarActions(
                        onToggleTranslation = {
                            callbacks.onSettingsAction(ReaderUiAction.SetShowTranslation(!showTranslation))
                        },
                        onOpenSettings = { showSettings = true },
                    ),
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
                    layout = uiState.layout,
                    settings = uiState.settings,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }

    if (showSettings && content != null) {
        ReaderSettingsSheet(
            settings = content.settings,
            onAction = callbacks.onSettingsAction,
            onDismiss = { showSettings = false },
            extras =
                ReaderSettingsExtras(
                    sholawatLayout =
                        SholawatLayoutControl(
                            pairingAvailable = content.layout == ContentLayout.BAYT,
                            twoColumn = content.settings.sholawatTwoColumn,
                            onTwoColumnChange = callbacks.onTwoColumnChange,
                            baitGap = content.settings.sholawatBaitGap,
                            onBaitGapChange = callbacks.onBaitGapChange,
                        ),
                ),
        )
    }
}

/** Bundles the top bar's two action callbacks so its parameter list stays within the shared limit. */
private data class SholawatTopBarActions(
    val onToggleTranslation: () -> Unit,
    val onOpenSettings: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SholawatReaderTopBar(
    title: String,
    hasContent: Boolean,
    showTranslation: Boolean,
    onBack: () -> Unit,
    actions: SholawatTopBarActions,
) {
    TopAppBar(
        title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            if (!hasContent) return@TopAppBar
            // The quick toggle stays in the bar — it is the one setting a reciter reaches for
            // mid-recitation — but it now writes to the same persisted preference the sheet's
            // switch does, so the two can never disagree.
            IconButton(onClick = actions.onToggleTranslation) {
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
            IconButton(onClick = actions.onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = stringResource(R.string.sholawat_reader_settings_content_description),
                )
            }
        },
    )
}

@Composable
private fun SholawatReaderStepList(
    steps: List<ContentStep>,
    layout: ContentLayout,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // Three independent vetoes, all narrowing: the CMS must call the item paired verse, the
        // reader must still fit two columns, and the user must not have turned pairing off.
        val pairHemistichs =
            layout == ContentLayout.BAYT && settings.sholawatTwoColumn && twoColumnsFit(maxWidth)
        val verticalGap =
            if (pairHemistichs && !settings.sholawatBaitGap) {
                SanguSantriSpacing.small
            } else {
                SanguSantriSpacing.large
            }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth),
            contentPadding = PaddingValues(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(verticalGap),
        ) {
            if (pairHemistichs) {
                // chunked(2) keeps every step: an odd count leaves a one-element final chunk,
                // which SholawatBaytRow renders alone in the right column.
                items(items = steps.chunked(BAYT_HEMISTICHS), key = { it.first().id }) { bayt ->
                    SholawatBaytRow(
                        bayt = bayt,
                        showTranslation = settings.showTranslation,
                        settings = settings,
                        arabicFont = settings.arabicFont,
                    )
                }
            } else {
                items(items = steps, key = { it.id }) { step ->
                    SholawatVerseBlock(
                        step = step,
                        showTranslation = settings.showTranslation,
                        settings = settings,
                        arabicFont = settings.arabicFont,
                    )
                }
            }
        }
    }
}

/**
 * Whether a bait actually has room for two columns, checked regardless of what the CMS said.
 *
 * Two independent reasons it may not. A narrow window leaves each hemistich too little width to
 * stay legible at all. And a large system font scale is a user instruction that
 * `docs/design/ACCESSIBILITY.md` requires be honoured at 1.5x — but half-width cells with an
 * auto-size floor and a two-line cap would answer it by shrinking the text back down, quietly
 * undoing the setting. Stacked reads correctly in both cases, so it wins in both.
 */
@Composable
private fun twoColumnsFit(availableWidth: Dp): Boolean =
    availableWidth >= MIN_TWO_COLUMN_WIDTH && LocalDensity.current.fontScale <= MAX_TWO_COLUMN_FONT_SCALE

private const val BAYT_HEMISTICHS = 2

// ponytail: fixed thresholds. MIN_TWO_COLUMN_WIDTH is a small phone's width less the reader's
// padding; MAX_TWO_COLUMN_FONT_SCALE sits below the 1.5x ACCESSIBILITY.md mandates so that scale
// always falls back. Tune both against real devices rather than adding a measurement pass.
private val MIN_TWO_COLUMN_WIDTH = 320.dp
private const val MAX_TWO_COLUMN_FONT_SCALE = 1.3f

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
        ContentStep(
            id = "fixture-2",
            contentId = "sholawat-fixture",
            position = 1,
            arabicText = "[FIXTURE] Teks Arab kedua.",
            translation = "[FIXTURE] Terjemahan kedua.",
            repeatTarget = 1,
        ),
    )

@PreviewLightDark
@Composable
private fun SholawatReaderScreenPreview() {
    SanguSantriTheme {
        SholawatReaderScreen(
            uiState =
                SholawatReaderUiState.ContentAvailable(
                    title = "[FIXTURE] Sholawat",
                    steps = previewSteps,
                    layout = ContentLayout.STACKED,
                    settings = ReaderSettings(arabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH),
                ),
            onBack = {},
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SholawatReaderScreenBaytPreview() {
    SanguSantriTheme {
        SholawatReaderScreen(
            uiState =
                SholawatReaderUiState.ContentAvailable(
                    title = "[FIXTURE] Qasidah",
                    steps = previewSteps,
                    layout = ContentLayout.BAYT,
                    settings = ReaderSettings(arabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH),
                ),
            onBack = {},
            onRetry = {},
        )
    }
}
