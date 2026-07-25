package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Generic local key-value store for application bootstrap metadata (for example,
 * seed content manifest version once content import lands). Feature-specific
 * tables are added alongside the features that need them.
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAtEpochMillis: Long,
)
