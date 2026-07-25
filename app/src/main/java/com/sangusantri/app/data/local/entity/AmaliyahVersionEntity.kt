package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.AmaliyahVersionStatus

/** Local mirror of the server `amaliyah_versions` table (PRD 11.1). Immutable once published (PRD 10.4). */
@Entity(
    tableName = "amaliyah_versions",
    foreignKeys = [
        ForeignKey(
            entity = AmaliyahVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variantId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ApprovalEntity::class,
            parentColumns = ["id"],
            childColumns = ["approvalId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["variantId"]),
        Index(value = ["approvalId"]),
        Index(value = ["variantId", "versionNumber"], unique = true),
    ],
)
data class AmaliyahVersionEntity(
    @PrimaryKey val id: String,
    val variantId: String,
    val versionNumber: Int,
    val schemaVersion: Int,
    val status: AmaliyahVersionStatus,
    val sourceName: String,
    val sourceReference: String,
    val approvalId: String,
    val checksumSha256: String,
    val minimumAppVersionCode: Int,
    val publishedAt: String?,
    val revokedAt: String?,
)
