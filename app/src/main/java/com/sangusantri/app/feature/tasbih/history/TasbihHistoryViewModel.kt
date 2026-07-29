package com.sangusantri.app.feature.tasbih.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.repository.TasbihRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TasbihHistoryViewModel
@Inject
constructor(
    repository: TasbihRepository,
) : ViewModel() {
    val uiState: StateFlow<TasbihHistoryUiState> =
        repository
            .observeHistory()
            .map { entries ->
                if (entries.isEmpty()) TasbihHistoryUiState.Empty else TasbihHistoryUiState.Filled(entries)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = TasbihHistoryUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
