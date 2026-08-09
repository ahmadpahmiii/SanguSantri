package com.sangusantri.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.SectionHeader
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.reminder.ReminderScheduleFormatter
import com.sangusantri.app.feature.update.AppUpdateGate

@Composable
fun SerambiRoute(
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    viewModel: SerambiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val screenActions = actions.copy(onDismissResume = viewModel::dismissResume)
    SerambiScreen(
        uiState = uiState,
        onContentSelected = onContentSelected,
        actions = screenActions,
        snackbarHostState = snackbarHostState,
    )
    AppUpdateGate(snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerambiScreen(
    uiState: SerambiUiState,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = actions.onSetelanClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.serambi_setelan_content_description),
                        )
                    }
                    IconButton(onClick = actions.onAboutClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.serambi_about_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            SerambiUiState.Loading ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is SerambiUiState.Loaded ->
                SerambiDashboard(
                    uiState = uiState,
                    onContentSelected = onContentSelected,
                    actions = actions,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun SerambiDashboard(
    uiState: SerambiUiState.Loaded,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
) {
    val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(SanguSantriDimensions.dashboardGridMinCellWidth),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = SanguSantriDimensions.dashboardContentMaxWidth),
            contentPadding = PaddingValues(SanguSantriSpacing.default),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            item(key = "greeting", span = { GridItemSpan(maxLineSpan) }) {
                SerambiGreeting()
            }

            if (uiState.items.isNotEmpty()) {
                item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                    SerambiSearchEntry(onClick = actions.onExploreClick)
                }
            }

            uiState.resumeItem?.let { resumeItem ->
                item(key = "resume", span = { GridItemSpan(maxLineSpan) }) {
                    SerambiResumeCard(
                        item = resumeItem,
                        actions = actions,
                        onDismiss = actions.onDismissResume,
                    )
                }
            }
            featureSection(uiState, actions)
            supportingFeatureSection(uiState, actions, hijriMonthNames)
            amaliyahSection(uiState, actions, onContentSelected)
        }
    }
}

private fun LazyGridScope.featureSection(
    uiState: SerambiUiState.Loaded,
    actions: SerambiActions,
) {
    item(key = "feature_spacer", span = { GridItemSpan(maxLineSpan) }) {
        Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
    }
    item(key = "feature_header", span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(title = stringResource(R.string.serambi_feature_section_title))
    }
    item(key = "main_features", span = { GridItemSpan(maxLineSpan) }) {
        SerambiMainFeatures(
            showAmaliyah = uiState.items.isNotEmpty(),
            actions = actions,
        )
    }
}

private fun LazyGridScope.supportingFeatureSection(
    uiState: SerambiUiState.Loaded,
    actions: SerambiActions,
    hijriMonthNames: List<String>,
) {
    item(key = "supporting_features", span = { GridItemSpan(maxLineSpan) }) {
        SerambiSupportingFeatures(
            reminderDescription =
                uiState.nearestReminder?.let { reminder ->
                    ReminderScheduleFormatter.formatScheduleSummary(reminder.schedule, hijriMonthNames)
                } ?: stringResource(R.string.serambi_reminder_feature_description),
            showNahwuQuiz = uiState.hasNahwuQuizContent,
            nahwuDescription =
                stringResource(
                    if (uiState.hasActiveNahwuQuiz) {
                        R.string.serambi_nahwu_supporting_resume
                    } else {
                        R.string.serambi_nahwu_supporting_choose
                    },
                ),
            actions = actions,
        )
    }
}

private fun LazyGridScope.amaliyahSection(
    uiState: SerambiUiState.Loaded,
    actions: SerambiActions,
    onContentSelected: (String) -> Unit,
) {
    if (uiState.featuredItems.isEmpty()) return
    item(key = "amaliyah_spacer", span = { GridItemSpan(maxLineSpan) }) {
        Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
    }
    item(key = "amaliyah_header", span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(
            title = stringResource(R.string.serambi_featured_amaliyah_title),
            actionLabel = stringResource(R.string.serambi_see_all_action),
            onActionClick = actions.onExploreClick,
        )
    }
    items(items = uiState.featuredItems, key = { "content_${it.id}" }) { item ->
        ContentCard(content = item, onClick = onContentSelected, compact = true)
    }
}

@Composable
private fun SerambiGreeting() {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        Text(
            text = stringResource(R.string.serambi_greeting),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.serambi_greeting_supporting),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Development-only preview fixtures — no religious text is invented.
private val previewItems =
    listOf(
        Content(
            id = "tahlil",
            title = "Tahlil",
            description = "[FIXTURE] Konten pengembangan.",
            imageUrl = null,
            category = "Tahlil dan Doa",
            version = 1,
            order = 1,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
        Content(
            id = "istighosah",
            title = "Istighosah",
            description = "[FIXTURE] Konten pengembangan.",
            imageUrl = null,
            category = "Tahlil dan Doa",
            version = 1,
            order = 2,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
    )

private val previewActions =
    SerambiActions(
        onSetelanClick = {},
        onAboutClick = {},
        onExploreClick = {},
        onPengingatClick = {},
        onBelajarClick = {},
        onQuranClick = {},
        onContinueAmaliyah = { _, _ -> },
        onContinueQuran = { _, _ -> },
        onContinueTasbih = {},
    )

@PreviewLightDark
@Composable
private fun SerambiScreenContentPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState =
                SerambiUiState.Loaded(
                    items = previewItems,
                    hasNahwuQuizContent = true,
                    hasActiveNahwuQuiz = true,
                    resumeItem =
                        SerambiResumeItem.Amaliyah(
                            contentId = "tahlil",
                            title = "Tahlil",
                            mode = ReaderMode.GUIDED,
                            current = 12,
                            total = 37,
                            lastActivityAtEpochMillis = 1L,
                        ),
                ),
            onContentSelected = {},
            actions = previewActions,
        )
    }
}

@Preview(name = "Empty catalogue")
@Composable
private fun SerambiScreenEmptyPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loaded(emptyList()),
            onContentSelected = {},
            actions = previewActions,
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun SerambiScreenLoadingPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loading,
            onContentSelected = {},
            actions = previewActions,
        )
    }
}
