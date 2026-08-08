package com.sangusantri.app.domain.model

/**
 * One local reading-activity event (QUR-FR-017), written only when the reader closes after the
 * position advanced by at least one ayat within [surahNumber] — merely opening and closing never
 * creates a session.
 */
data class QuranReadingSession(
    val id: Long,
    val surahNumber: Int,
    val startAyat: Int,
    val endAyat: Int,
    val readAtEpochMillis: Long,
)
