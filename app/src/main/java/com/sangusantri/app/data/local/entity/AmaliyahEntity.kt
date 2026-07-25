package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Local mirror of the server `amaliyah` table (PRD 11.1), content-hierarchy fields only. */
@Entity(
    tableName = "amaliyah",
    indices = [Index(value = ["slug"], unique = true)],
)
data class AmaliyahEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val titleId: String,
    val titleAr: String,
    val descriptionId: String?,
    val descriptionAr: String?,
    val category: String,
)
