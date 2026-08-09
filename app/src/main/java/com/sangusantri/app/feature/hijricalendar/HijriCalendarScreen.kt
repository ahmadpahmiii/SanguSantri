package com.sangusantri.app.feature.hijricalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.HijriAgendaCalculator
import com.sangusantri.app.domain.model.HijriMonthGridCalculator
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarAgendaSection
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarEventDetailDialog
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarGrid
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarMonthHeader
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarMonthNavigation
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarSelectedSummary
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarSourceSheet
import com.sangusantri.app.feature.hijricalendar.components.HijriCalendarWeekdayRow
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HijriCalendarRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HijriCalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HijriCalendarScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    uiState: HijriCalendarUiState,
    onAction: (HijriCalendarUiAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { HijriCalendarTopBar(uiState = uiState, onAction = onAction, onBack = onBack) },
    ) { innerPadding ->
        HijriCalendarContent(uiState = uiState, onAction = onAction, modifier = Modifier.padding(innerPadding))
    }

    if (uiState.isSourceSheetVisible) {
        HijriCalendarSourceSheet(onDismiss = { onAction(HijriCalendarUiAction.DismissSourceSheet) })
    }

    uiState.detailEvent?.let { event ->
        HijriCalendarEventDetailDialog(
            event = event,
            onDismiss = { onAction(HijriCalendarUiAction.DismissEventDetail) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HijriCalendarTopBar(
    uiState: HijriCalendarUiState,
    onAction: (HijriCalendarUiAction) -> Unit,
    onBack: () -> Unit,
) {
    val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        title = {
            Column {
                Text(text = stringResource(R.string.hijri_calendar_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = HijriCalendarFormatter.formatSelectedDateSubtitle(uiState.selectedDay, hijriMonthNames),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            TextButton(onClick = { onAction(HijriCalendarUiAction.GoToToday) }) {
                Text(text = stringResource(R.string.hijri_calendar_today_action))
            }
            IconButton(onClick = { onAction(HijriCalendarUiAction.OpenSourceSheet) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.hijri_calendar_source_action_content_description),
                )
            }
        },
    )
}

@Composable
private fun HijriCalendarContent(
    uiState: HijriCalendarUiState,
    onAction: (HijriCalendarUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                .padding(horizontal = SanguSantriSpacing.default),
    ) {
        HijriCalendarMonthHeader(
            month = uiState.month,
            navigation =
                HijriCalendarMonthNavigation(
                    canGoToPrevious = uiState.canGoToPreviousMonth,
                    canGoToNext = uiState.canGoToNextMonth,
                    onPrevious = { onAction(HijriCalendarUiAction.GoToPreviousMonth) },
                    onNext = { onAction(HijriCalendarUiAction.GoToNextMonth) },
                ),
        )
        HijriCalendarWeekdayRow()
        HijriCalendarGrid(
            days = uiState.month.days,
            selectedDate = uiState.selectedDate,
            onDaySelected = { date -> onAction(HijriCalendarUiAction.SelectDate(date)) },
            modifier = Modifier.padding(top = SanguSantriSpacing.small),
        )
        HijriCalendarSelectedSummary(day = uiState.selectedDay)
        HijriCalendarAgendaSection(
            events = uiState.filteredAgendaEvents,
            filter = uiState.agendaFilter,
            onFilterSelected = { filter -> onAction(HijriCalendarUiAction.SelectFilter(filter)) },
            onEventInfoClick = { event -> onAction(HijriCalendarUiAction.ShowEventDetail(event.id)) },
            modifier = Modifier.padding(bottom = SanguSantriSpacing.large),
        )
    }
}

@PreviewLightDark
@Composable
private fun HijriCalendarScreenPreview() {
    SanguSantriTheme {
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 8)
        HijriCalendarScreen(
            uiState =
                HijriCalendarUiState(
                    month = HijriMonthGridCalculator.build(yearMonth, today),
                    selectedDate = today,
                    agendaEvents = HijriAgendaCalculator.eventsForGregorianMonth(yearMonth),
                    agendaFilter = HijriCalendarAgendaFilter.ALL,
                    isSourceSheetVisible = false,
                    canGoToPreviousMonth = true,
                    canGoToNextMonth = true,
                ),
            onAction = {},
            onBack = {},
        )
    }
}
