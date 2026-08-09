package com.sangusantri.app.feature.quran

/** States for the Al-Qur'an Kemenag entry gate (`docs/product/QURAN_PRD.md` §6.1). [Ready] is
 * transient — [QuranEntryRoute] reacts to it and the NavHost replaces this gate with the hub. */
sealed interface QuranEntryUiState {
    data object Checking : QuranEntryUiState

    data class Preparing(
        val completed: Int,
        val total: Int,
    ) : QuranEntryUiState

    data class PreparationFailed(
        val reason: String,
    ) : QuranEntryUiState

    data object OfflineNoLocalData : QuranEntryUiState

    data object Ready : QuranEntryUiState
}
