package com.sangusantri.app.feature.activity.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.core.designsystem.component.TimeRangeFilter
import com.sangusantri.app.core.designsystem.component.filterByTimeRange
import com.sangusantri.app.domain.model.QuranActivityEntry
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** "Lihat semua" for Aktivitas' (`0.0.6`) Quran-reading-history section — reuses [QuranRepository]'s
 * existing reading-session data directly, resolving surah names the same way
 * `ObserveActivityOverviewUseCase` does. */
@HiltViewModel
class ActivityQuranHistoryViewModel
@Inject
constructor(
    private val quranRepository: QuranRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(TimeRangeFilter.ALL)

    val uiState: StateFlow<ActivityQuranHistoryUiState> =
        combine(
            quranRepository.observeReadingSessions(),
            quranRepository.observeSurahs(),
            filter,
        ) { sessions, surahs, range ->
            val surahNames = surahs.associate { it.number to it.latinName }
            val entries =
                sessions
                    .filterByTimeRange(range, System.currentTimeMillis()) { it.readAtEpochMillis }
                    .map { session ->
                        QuranActivityEntry(
                            surahNumber = session.surahNumber,
                            surahName = surahNames[session.surahNumber].orEmpty(),
                            startAyat = session.startAyat,
                            endAyat = session.endAyat,
                            readAtEpochMillis = session.readAtEpochMillis,
                        )
                    }
            ActivityQuranHistoryUiState(filter = range, entries = entries)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ActivityQuranHistoryUiState(),
        )

    fun onFilterSelected(range: TimeRangeFilter) {
        filter.value = range
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
