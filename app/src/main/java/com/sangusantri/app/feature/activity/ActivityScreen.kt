package com.sangusantri.app.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sangusantri.app.feature.activity.components.ActivityHistorySection
import com.sangusantri.app.feature.activity.components.ActivityStreakSection
import com.sangusantri.app.feature.activity.components.ActivityWeeklySection

@Composable
fun ActivityRoute(
    onAmaliyahHistoryClick: () -> Unit,
    onTasbihHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityScreen(
        uiState = uiState,
        onAmaliyahHistoryClick = onAmaliyahHistoryClick,
        onTasbihHistoryClick = onTasbihHistoryClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    uiState: ActivityUiState,
    onAmaliyahHistoryClick: () -> Unit,
    onTasbihHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.activity_title)) }) },
    ) { innerPadding ->
        when (uiState) {
            ActivityUiState.Loading ->
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is ActivityUiState.Content ->
                ActivityContent(
                    overview = uiState.overview,
                    onAmaliyahHistoryClick = onAmaliyahHistoryClick,
                    onTasbihHistoryClick = onTasbihHistoryClick,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun ActivityContent(
    overview: ActivityOverview,
    onAmaliyahHistoryClick: () -> Unit,
    onTasbihHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (overview.isEntirelyEmpty) {
            ActivityEmptyState(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
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
            if (overview.hasStreak) ActivityStreakSection(overview)
            if (overview.hasWeeklyActivity) ActivityWeeklySection(overview)
            if (overview.hasAmaliyahHistory) {
                ActivityHistorySection(
                    title = stringResource(R.string.activity_section_amaliyah_history),
                    kind = ActivityRowKind.AMALIYAH,
                    rows = overview.recentAmaliyahCompletions.map { it.toRowContent() },
                    onSeeAllClick = onAmaliyahHistoryClick,
                )
            }
            if (overview.hasTasbihHistory) {
                ActivityHistorySection(
                    title = stringResource(R.string.activity_section_tasbih_history),
                    kind = ActivityRowKind.TASBIH,
                    rows = overview.recentTasbihHistory.map { it.toRowContent() },
                    onSeeAllClick = onTasbihHistoryClick,
                )
            }
        }
    }
}

@Composable
private fun ActivityEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(SanguSantriSpacing.large),
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

@PreviewLightDark
@Composable
private fun ActivityScreenEmptyPreview() {
    SanguSantriTheme {
        ActivityScreen(
            uiState =
                ActivityUiState.Content(
                    ActivityOverview(
                        currentStreakDays = 0,
                        longestStreakDays = 0,
                        weeklyAmaliyahCompletedCount = 0,
                        weeklyTasbihSessionCount = 0,
                        weeklyTotalMinutes = 0,
                        recentAmaliyahCompletions = emptyList(),
                        recentTasbihHistory = emptyList(),
                    ),
                ),
            onAmaliyahHistoryClick = {},
            onTasbihHistoryClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityScreenLoadingPreview() {
    SanguSantriTheme {
        ActivityScreen(uiState = ActivityUiState.Loading, onAmaliyahHistoryClick = {}, onTasbihHistoryClick = {})
    }
}
