package com.sangusantri.app.feature.tasbih

import com.sangusantri.app.domain.model.TasbihTargetPreset

/** Standalone Tasbih screen state (0.0.2). One `Active` shape covers states 2/3/4/9 of the design
 * spec (Sesi Aktif/Target Tercapai/Target Tanpa Batas/Sesi Dipulihkan) — they are all the same
 * session data with different derived flags, not distinct screens. */
sealed interface TasbihUiState {
    /** No session exists in Room yet (state 1, "Belum Ada Sesi"). */
    data object NoSession : TasbihUiState

    data class Active(
        val currentCount: Int,
        val targetValue: Int?,
        val targetPreset: TasbihTargetPreset,
        val sessionName: String?,
        val isTargetReached: Boolean,
        /**
         * True only for the first UI state emitted by this `TasbihViewModel` instance when that
         * first-loaded session already had a positive count — i.e. genuinely restored from Room,
         * not just-created by a user action within this same screen visit. Clears itself the
         * moment the count changes (design spec state 9: "transient, shown once per cold start,
         * not persistent chrome").
         */
        val isRestored: Boolean,
    ) : TasbihUiState
}
