package com.sangusantri.app.feature.tasbih.history

import com.sangusantri.app.domain.model.TasbihHistoryEntry

/** Tasbih Session History screen state (0.0.2, states 7/8 "Riwayat Kosong"/"Riwayat Terisi"). */
sealed interface TasbihHistoryUiState {
    data object Loading : TasbihHistoryUiState

    data object Empty : TasbihHistoryUiState

    data class Filled(
        val entries: List<TasbihHistoryEntry>,
    ) : TasbihHistoryUiState
}
