package com.sangusantri.app.domain.model

/** One [QuranReadingSession] resolved with its surah's display name, for Aktivitas (QUR-FR-017) —
 * a presentation-ready combination of two repositories' data, not a duplicate persisted model. */
data class QuranActivityEntry(
    val surahNumber: Int,
    val surahName: String,
    val startAyat: Int,
    val endAyat: Int,
    val readAtEpochMillis: Long,
)
