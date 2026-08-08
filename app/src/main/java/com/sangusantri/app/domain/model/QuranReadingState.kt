package com.sangusantri.app.domain.model

/** The single global last-read position (QUR-FR-011). At most one instance ever exists. */
data class QuranReadingState(
    val surahNumber: Int,
    val ayatNumber: Int,
    val page: Int,
    val updatedAtEpochMillis: Long,
)
