package com.sangusantri.app.domain.model

/**
 * The single active Standalone Tasbih session (0.0.2, `CONTENT_MODEL.md`'s forward-documented
 * `tasbih_sessions` shape). [targetValue] is `null` when [targetPreset] is
 * [TasbihTargetPreset.UNLIMITED] — an unlimited session has no completion ceiling. Persisted
 * automatically so an unfinished count survives an app restart.
 */
data class TasbihSession(
    val currentCount: Int,
    val targetValue: Int?,
    val targetPreset: TasbihTargetPreset,
    val sessionName: String?,
    val startedAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
) {
    val isTargetReached: Boolean
        get() = targetValue != null && currentCount >= targetValue
}
