package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.Approval
import com.sangusantri.app.domain.model.ReaderSettings

/** Full Reader screen state (Milestone 3). */
sealed interface ReaderUiState {
    data object Loading : ReaderUiState

    data class ContentAvailable(
        val amaliyahTitleId: String,
        val versionId: String,
        val steps: List<AmaliyahStep>,
        val settings: ReaderSettings,
        val initialItemIndex: Int,
        val initialItemOffset: Int,
        val approval: Approval,
    ) : ReaderUiState

    /** No amaliyah for the slug, or no [com.sangusantri.app.domain.model.AmaliyahVersionStatus.PUBLISHED] version. */
    data object ContentUnavailable : ReaderUiState

    /** An unexpected load failure the user can retry — never a raw exception message. */
    data object RecoverableError : ReaderUiState
}
