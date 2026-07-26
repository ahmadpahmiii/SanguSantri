package com.sangusantri.app.feature.guidedreader

/** User-initiated intents the Guided Reader sends to [GuidedReaderViewModel] (unidirectional data flow). */
sealed interface GuidedReaderUiAction {
    data object Previous : GuidedReaderUiAction

    data object Continue : GuidedReaderUiAction

    data object IncrementCounter : GuidedReaderUiAction

    /** Confirmed via a dialog in the UI layer — the ViewModel never resets without confirmation. */
    data object ResetCounter : GuidedReaderUiAction

    /** Confirmed via a dialog in the UI layer — the final completion action (FR-007). */
    data object ConfirmCompletion : GuidedReaderUiAction

    data object Retry : GuidedReaderUiAction

    /** Switches to the Full Reader at the current step's item index (FR-016). */
    data object SwitchToFull : GuidedReaderUiAction

    /** Jump to a Table of Contents section (FR-017) — never marks skipped content complete. */
    data class JumpToStep(
        val stepId: String,
    ) : GuidedReaderUiAction
}
