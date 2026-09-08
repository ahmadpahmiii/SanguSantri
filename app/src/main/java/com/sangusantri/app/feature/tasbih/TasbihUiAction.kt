package com.sangusantri.app.feature.tasbih

import com.sangusantri.app.domain.model.TasbihTargetPreset

/** User-initiated intents the Tasbih screen sends to [TasbihViewModel] (unidirectional data flow). */
sealed interface TasbihUiAction {
    data object IncrementCounter : TasbihUiAction

    /** 33/100/Unlimited only — [TasbihTargetPreset.CUSTOM] is handled by [SetCustomTarget] instead. */
    data class SelectPreset(val preset: TasbihTargetPreset) : TasbihUiAction

    /** Dispatched only after the Custom Target Dialog's own validation already accepted [value]. */
    data class SetCustomTarget(val value: Int) : TasbihUiAction

    data class RenameSession(val name: String?) : TasbihUiAction

    /**
     * Start the same target over from zero after it was reached. The finished round is already in
     * Riwayat by this point, so this discards nothing.
     */
    data object RepeatRound : TasbihUiAction

    /**
     * End the session: archive it to Riwayat and clear the counter. Confirmed via a dialog in the
     * UI layer — the ViewModel never ends a session without confirmation.
     */
    data object FinishSession : TasbihUiAction
}
