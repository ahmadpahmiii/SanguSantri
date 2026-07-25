package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.Visibility

/** Local mirror of the server `amaliyah_variants` table (PRD 11.1). */
@Entity(
    tableName = "amaliyah_variants",
    foreignKeys = [
        ForeignKey(
            entity = AmaliyahEntity::class,
            parentColumns = ["id"],
            childColumns = ["amaliyahId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["amaliyahId"]),
        Index(value = ["amaliyahId", "slug"], unique = true),
    ],
)
data class AmaliyahVariantEntity(
    @PrimaryKey val id: String,
    val amaliyahId: String,
    val slug: String,
    val nameId: String,
    val nameAr: String,
    val ownerType: OwnerType,
    val pondokId: String?,
    val visibility: Visibility,
    val isDefault: Boolean,
)
