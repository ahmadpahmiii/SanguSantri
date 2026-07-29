package com.sangusantri.app.feature.tasbih

import com.sangusantri.app.domain.model.TasbihTargetPreset

/** User-initiated intents the Tasbih screen sends to [TasbihViewModel] (unidirectional data flow). */
sealed interface TasbihUiAction {
    data object IncrementCounter : TasbihUiAction

    /** 33/100/Unlimited only — [TasbihTargetPreset.CUSTOM] is handled by [SetCustomTarget] instead. */
    data class SelectPreset(
        val preset: TasbihTargetPreset,
    ) : TasbihUiAction

    /** Dispatched only after the Custom Target Dialog's own validation already accepted [value]. */
    data class SetCustomTarget(
        val value: Int,
    ) : TasbihUiAction

    data class RenameSession(
        val name: String?,
    ) : TasbihUiAction

    /** Confirmed via a dialog in the UI layer — the ViewModel never resets without confirmation. */
    data object ResetSession : TasbihUiAction
}
