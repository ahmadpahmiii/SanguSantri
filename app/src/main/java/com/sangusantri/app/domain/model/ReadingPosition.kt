package com.sangusantri.app.domain.model

/**
 * The reader's last visible position within one [Content] item (Milestone 3 minimum scope).
 * Deliberately narrower than the `reading_sessions` table sketched in
 * `docs/engineering/CONTENT_MODEL.md` — that design carries guided-mode and completion fields
 * that belong to a later milestone; this model is not a completion or history record.
 */
data class ReadingPosition(
    val contentId: String,
    val itemIndex: Int,
    val itemOffset: Int,
    val lastOpenedAtEpochMillis: Long,
)
