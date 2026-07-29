package com.sangusantri.app.feature.activity.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.core.designsystem.component.ActivityRowKind
import com.sangusantri.app.core.designsystem.component.TimeRangeFilterState
import com.sangusantri.app.feature.activity.toRowContent

@Composable
fun ActivityTasbihHistoryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityTasbihHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityHistoryDetailScaffold(
        kind = ActivityRowKind.TASBIH,
        filterState = TimeRangeFilterState(selected = uiState.filter, onSelect = viewModel::onFilterSelected),
        rows = uiState.entries.map { it.toRowContent() },
        onBack = onBack,
        modifier = modifier,
    )
}
