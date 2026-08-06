package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The reader's last visible position for one [ContentEntity] item (Milestone 3 minimum
 * scope — see [com.sangusantri.app.domain.model.ReadingPosition]). One row per [contentId]: a
 * content update replaces steps in place, so this position is retained by content update (only
 * a fresh sync/bootstrap explicitly resets it when the step list has genuinely changed).
 */
@Entity(tableName = "reading_positions")
data class ReadingPositionEntity(
    @PrimaryKey val contentId: String,
    val itemIndex: Int,
    val itemOffset: Int,
    val lastOpenedAtEpochMillis: Long,
)
