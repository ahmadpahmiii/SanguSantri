package com.sangusantri.app.domain.model

/** One archived Standalone Tasbih session (0.0.2), recorded when the user confirms a reset. */
data class TasbihHistoryEntry(
    val id: Long,
    val sessionName: String?,
    val targetValue: Int?,
    val finalCount: Int,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long,
)
