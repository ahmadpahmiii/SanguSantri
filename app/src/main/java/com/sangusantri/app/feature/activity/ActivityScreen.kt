package com.sangusantri.app.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ActivityRowKind
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ActivityOverview
import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.feature.activity.components.ActivityHistorySection
import com.sangusantri.app.feature.activity.components.ActivityWeeklySection
import com.sangusantri.app.feature.activity.components.AmalanHarianCard
import com.sangusantri.app.feature.activity.components.AmalanMilestoneSheet

@Suppress("LongParameterList")
@Composable
fun ActivityRoute(
    onAmaliyahHistoryClick: () -> Unit,
    onTasbihHistoryClick: () -> Unit,
    onRemindersClick: () -> Unit,
    onQuranHistoryClick: () -> Unit,
    onDzikirClick: () -> Unit,
    onQuranClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityScreen(
        uiState = uiState,
        actions =
            ActivityActions(
                onAmaliyahHistoryClick = onAmaliyahHistoryClick,
                onTasbihHistoryClick = onTasbihHistoryClick,
                onRemindersClick = onRemindersClick,
                onQuranHistoryClick = onQuranHistoryClick,
                onDzikirClick = onDzikirClick,
                onQuranClick = onQuranClick,
                onUdzurChange = viewModel::setUdzur,
                onMilestoneDismissed = viewModel::dismissMilestone,
            ),
        modifier = modifier,
    )
}

/** Aktivitas' user actions, grouped so the screen keeps one parameter instead of eight. */
data class ActivityActions(
    val onAmaliyahHistoryClick: () -> Unit,
    val onTasbihHistoryClick: () -> Unit,
    val onRemindersClick: () -> Unit,
    val onQuranHistoryClick: () -> Unit,
    val onDzikirClick: () -> Unit,
    val onQuranClick: () -> Unit,
    val onUdzurChange: (Boolean) -> Unit,
    val onMilestoneDismissed: (Int) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    uiState: ActivityUiState,
    actions: ActivityActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.activity_title)) }) },
    ) { innerPadding ->
        when (uiState) {
            ActivityUiState.Loading ->
                Box(
                    modifier =
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is ActivityUiState.Content -> {
                ActivityContent(
                    state = uiState,
                    actions = actions,
                    modifier = Modifier.padding(innerPadding),
                )
                uiState.pendingMilestone?.let { milestone ->
                    AmalanMilestoneSheet(
                        streakDays = milestone,
                        recordDays = uiState.amalan.longestStreakDays,
                        onDismiss = { actions.onMilestoneDismissed(milestone) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityContent(
    state: ActivityUiState.Content,
    actions: ActivityActions,
    modifier: Modifier = Modifier,
) {
    val overview = state.overview
    Box(modifier = modifier.fillMaxSize()) {
        val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .align(Alignment.TopCenter)
                    .padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
        ) {
            // Always rendered, even on a brand-new install: a hidden goal card can never start a
            // streak, which is the one thing this screen exists to do.
            AmalanHarianCard(
                state = state.amalan,
                onDzikirClick = actions.onDzikirClick,
                onQuranClick = actions.onQuranClick,
                onUdzurChange = actions.onUdzurChange,
            )
            if (overview.isEntirelyEmpty) {
                ActivityEmptyState()
                return@Column
            }
            if (overview.hasWeeklyActivity) ActivityWeeklySection(overview)
            ActivityHistorySections(overview = overview, actions = actions, hijriMonthNames = hijriMonthNames)
        }
    }
}

/** The four "Lihat semua" preview lists, split out of [ActivityContent] to keep it readable. */
@Composable
private fun ActivityHistorySections(
    overview: ActivityOverview,
    actions: ActivityActions,
    hijriMonthNames: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large)) {
        if (overview.hasAmaliyahHistory) {
            ActivityHistorySection(
                title = stringResource(R.string.activity_section_amaliyah_history),
                kind = ActivityRowKind.AMALIYAH,
                rows = overview.recentAmaliyahCompletions.map { it.toRowContent() },
                onSeeAllClick = actions.onAmaliyahHistoryClick,
            )
        }
        if (overview.hasTasbihHistory) {
            ActivityHistorySection(
                title = stringResource(R.string.activity_section_tasbih_history),
                kind = ActivityRowKind.TASBIH,
                rows = overview.recentTasbihHistory.map { it.toRowContent() },
                onSeeAllClick = actions.onTasbihHistoryClick,
            )
        }
        if (overview.hasReminders) {
            ActivityHistorySection(
                title = stringResource(R.string.activity_section_reminders),
                kind = ActivityRowKind.REMINDER,
                rows = overview.upcomingReminders.map { it.toRowContent(hijriMonthNames) },
                onSeeAllClick = actions.onRemindersClick,
            )
        }
        if (overview.hasQuranHistory) {
            ActivityHistorySection(
                title = stringResource(R.string.activity_section_quran_history),
                kind = ActivityRowKind.QURAN,
                rows = overview.recentQuranSessions.map { it.toRowContent() },
                onSeeAllClick = actions.onQuranHistoryClick,
            )
        }
    }
}

@Composable
private fun ActivityEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SanguSantriSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = stringResource(R.string.activity_empty_heading), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.activity_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val previewActions =
    ActivityActions(
        onAmaliyahHistoryClick = {},
        onTasbihHistoryClick = {},
        onRemindersClick = {},
        onQuranHistoryClick = {},
        onDzikirClick = {},
        onQuranClick = {},
        onUdzurChange = {},
        onMilestoneDismissed = {},
    )

@PreviewLightDark
@Composable
private fun ActivityScreenEmptyPreview() {
    val today = remember { java.time.LocalDate.now() }
    SanguSantriTheme {
        ActivityScreen(
            uiState =
                ActivityUiState.Content(
                    overview =
                        ActivityOverview(
                            weeklyAmaliyahCompletedCount = 0,
                            weeklyTasbihSessionCount = 0,
                            weeklyTotalMinutes = 0,
                            recentAmaliyahCompletions = emptyList(),
                            recentTasbihHistory = emptyList(),
                        ),
                    amalan =
                        AmalanHarian(
                            dzikirDone = false,
                            quranPagesToday = 0,
                            isUdzurToday = false,
                            currentStreakDays = 0,
                            longestStreakDays = 0,
                            week =
                                (6 downTo 0).map { back ->
                                    AmalanDay(
                                        date = today.minusDays(back.toLong()),
                                        state = if (back == 0) AmalanDayState.PENDING else AmalanDayState.INCOMPLETE,
                                    )
                                },
                            hasEverCompleted = false,
                        ),
                ),
            actions = previewActions,
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityScreenLoadingPreview() {
    SanguSantriTheme {
        ActivityScreen(uiState = ActivityUiState.Loading, actions = previewActions)
    }
}
