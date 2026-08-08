package com.sangusantri.app.domain.model

/** One local, ayat-level bookmark (QUR-FR-012) — no folders, notes, or cloud sync. */
data class QuranBookmark(
    val surahNumber: Int,
    val ayatNumber: Int,
    val createdAtEpochMillis: Long,
)
