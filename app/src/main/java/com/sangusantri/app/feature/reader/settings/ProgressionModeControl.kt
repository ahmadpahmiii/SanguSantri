package com.sangusantri.app.feature.reader.settings

import com.sangusantri.app.domain.model.GuidedProgressionMode

/**
 * Bundles the Guided Reader-only progression-mode control so [ReaderSettingsSheet] stays under the
 * shared-parameter-count limit while still being reused (not duplicated) by both readers — `null`
 * when opened from the Full Reader, which has no progression concept.
 */
data class ProgressionModeControl(
    val mode: GuidedProgressionMode,
    val onChange: (GuidedProgressionMode) -> Unit,
)
