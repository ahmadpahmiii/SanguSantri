package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One catalog item — local mirror of a `content-hosting/` catalog entry (ADR 0015). */
@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: String?,
    val version: Int,
    val order: Int,
    val isActive: Boolean,
    val sourceName: String,
    val sourceUrl: String,
)
