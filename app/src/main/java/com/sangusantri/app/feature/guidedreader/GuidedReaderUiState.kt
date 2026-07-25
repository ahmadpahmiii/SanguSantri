package com.sangusantri.app.feature.guidedreader

import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.ReaderSettings

/** Guided Reader screen state (Milestone 4, FR-005/FR-006/FR-007) — one step visible at a time. */
sealed interface GuidedReaderUiState {
    data object Loading : GuidedReaderUiState

    data class StepVisible(
        val amaliyahTitleId: String,
        val versionId: String,
        val step: AmaliyahStep,
        val stepIndex: Int,
        val stepCount: Int,
        val currentCount: Int,
        val settings: ReaderSettings,
        val isFirstStep: Boolean,
        val isLastStep: Boolean,
        val continueEnabled: Boolean,
        val allRequiredCountersComplete: Boolean,
        val isCompleted: Boolean,
    ) : GuidedReaderUiState

    /** No amaliyah for the slug, or no published version — same handling as the Full Reader. */
    data object ContentUnavailable : GuidedReaderUiState

    /** An unexpected load failure the user can retry — never a raw exception message. */
    data object RecoverableError : GuidedReaderUiState
}
