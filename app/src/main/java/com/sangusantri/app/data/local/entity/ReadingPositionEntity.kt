package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The reader's last visible position for one immutable content version (Milestone 3 minimum
 * scope — see [com.sangusantri.app.domain.model.ReadingPosition]). One row per [versionId]: a
 * new content version (a correction) naturally starts its own position rather than inheriting
 * the previous version's.
 */
@Entity(tableName = "reading_positions")
data class ReadingPositionEntity(
    @PrimaryKey val versionId: String,
    val itemIndex: Int,
    val itemOffset: Int,
    val lastOpenedAtEpochMillis: Long,
)
