package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sangusantri.app.domain.model.ApprovalStatus

/**
 * Local mirror of the server `approvals` table (PRD 6.5, 6.6, 11.1). The private
 * signed document (`document_storage_key`) is intentionally not mirrored on-device.
 */
@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val id: String,
    val approverName: String,
    val approverRole: String,
    val institutionName: String?,
    val approvalDate: String,
    val approvalScope: String,
    val publicDocumentStorageKey: String?,
    val documentReferenceNumber: String?,
    val status: ApprovalStatus,
)
