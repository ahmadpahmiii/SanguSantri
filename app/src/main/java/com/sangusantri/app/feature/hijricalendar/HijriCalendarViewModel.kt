package com.sangusantri.app.feature.hijricalendar

import androidx.lifecycle.ViewModel
import com.sangusantri.app.domain.model.HijriAgendaCalculator
import com.sangusantri.app.domain.model.HijriMonthGridCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * No repository dependency — every value is a pure, synchronous local calculation
 * ([HijriMonthGridCalculator], [HijriAgendaCalculator]), matching the PRD's explicit "does not
 * require Room" decision (§5.3). Still `@HiltViewModel` for the same `hiltViewModel()` construction
 * convention every other screen uses.
 */
@HiltViewModel
class HijriCalendarViewModel
    @Inject
    constructor() : ViewModel() {
        private val today: LocalDate = LocalDate.now()
        private val minYearMonth: YearMonth = YearMonth.from(today).minusYears(BROWSE_RANGE_YEARS)
        private val maxYearMonth: YearMonth = YearMonth.from(today).plusYears(BROWSE_RANGE_YEARS)

        private val initialYearMonth = YearMonth.from(today)
        private val _uiState =
            MutableStateFlow(
                HijriCalendarUiState(
                    month = HijriMonthGridCalculator.build(initialYearMonth, today),
                    selectedDate = today,
                    agendaEvents = HijriAgendaCalculator.eventsForGregorianMonth(initialYearMonth),
                    agendaFilter = HijriCalendarAgendaFilter.ALL,
                    isSourceSheetVisible = false,
                    canGoToPreviousMonth = initialYearMonth > minYearMonth,
                    canGoToNextMonth = initialYearMonth < maxYearMonth,
                ),
            )
        val uiState: StateFlow<HijriCalendarUiState> = _uiState.asStateFlow()

        fun onAction(action: HijriCalendarUiAction) {
            when (action) {
                is HijriCalendarUiAction.SelectDate -> selectDate(action.date)
                HijriCalendarUiAction.GoToPreviousMonth -> navigateMonth(-1L)
                HijriCalendarUiAction.GoToNextMonth -> navigateMonth(1L)
                HijriCalendarUiAction.GoToToday -> applyMonth(initialYearMonth, today)
                is HijriCalendarUiAction.SelectFilter -> _uiState.update { it.copy(agendaFilter = action.filter) }
                HijriCalendarUiAction.OpenSourceSheet -> _uiState.update { it.copy(isSourceSheetVisible = true) }
                HijriCalendarUiAction.DismissSourceSheet -> _uiState.update { it.copy(isSourceSheetVisible = false) }
                is HijriCalendarUiAction.ShowEventDetail -> _uiState.update { it.copy(detailEventId = action.eventId) }
                HijriCalendarUiAction.DismissEventDetail -> _uiState.update { it.copy(detailEventId = null) }
            }
        }

        /** Same-month selection is a cheap in-place update; selecting a muted adjacent-month cell
         * moves to that month while preserving the tapped date (PRD §6). */
        private fun selectDate(date: LocalDate) {
            val targetYearMonth = YearMonth.from(date)
            if (targetYearMonth == _uiState.value.month.yearMonth) {
                _uiState.update { it.copy(selectedDate = date) }
            } else {
                applyMonth(targetYearMonth, date)
            }
        }

        /** Keeps the same day-of-month across a prev/next navigation, clamped to the target month's
         * length; does nothing past the ten-year browse boundary (§4.1, disabled-direction state). */
        private fun navigateMonth(deltaMonths: Long) {
            val targetYearMonth =
                _uiState.value.month.yearMonth
                    .plusMonths(deltaMonths)
            if (targetYearMonth < minYearMonth || targetYearMonth > maxYearMonth) return
            val clampedDay = minOf(_uiState.value.selectedDate.dayOfMonth, targetYearMonth.lengthOfMonth())
            applyMonth(targetYearMonth, targetYearMonth.atDay(clampedDay))
        }

        private fun applyMonth(
            yearMonth: YearMonth,
            selectedDate: LocalDate,
        ) {
            _uiState.update {
                it.copy(
                    month = HijriMonthGridCalculator.build(yearMonth, today),
                    selectedDate = selectedDate,
                    agendaEvents = HijriAgendaCalculator.eventsForGregorianMonth(yearMonth),
                    canGoToPreviousMonth = yearMonth > minYearMonth,
                    canGoToNextMonth = yearMonth < maxYearMonth,
                )
            }
        }

        private companion object {
            const val BROWSE_RANGE_YEARS = 10L
        }
    }
