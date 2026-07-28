package com.sangusantri.app.data.content.dto

import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
import kotlinx.serialization.Serializable

/**
 * One content package JSON (PRD 10.1, 12.2, content-schema.md): a single immutable amaliyah
 * version with its variant, approval, and ordered steps. Shared verbatim between bundled assets
 * and the backend package endpoint (`GET /v1/content/packages/{versionId}`) — [ContentPackageImporter]
 * does not know which transport a given instance came from. `variantId`/`versionId` foreign keys
 * are not repeated in the payload — the importer derives them from the nesting when mapping to
 * Room entities.
 */
@Serializable
data class ContentPackageDto(
    val schemaVersion: Int,
    val amaliyah: AmaliyahDto,
    val variant: AmaliyahVariantDto,
    val version: AmaliyahVersionDto,
    val approval: ApprovalDto,
    val steps: List<AmaliyahStepDto>,
)

@Serializable
data class AmaliyahDto(
    val id: String,
    val slug: String,
    val titleId: String,
    val titleAr: String,
    val descriptionId: String? = null,
    val descriptionAr: String? = null,
    val category: String,
)

@Serializable
data class AmaliyahVariantDto(
    val id: String,
    val slug: String,
    val nameId: String,
    val nameAr: String,
    val ownerType: OwnerType,
    val pondokId: String? = null,
    val visibility: Visibility,
    val isDefault: Boolean,
)

@Serializable
data class AmaliyahVersionDto(
    val id: String,
    val versionNumber: Int,
    val status: AmaliyahVersionStatus,
    val sourceName: String,
    val sourceReference: String,
    val minimumAppVersionCode: Int,
    val publishedAt: String? = null,
    val revokedAt: String? = null,
)

@Serializable
data class ApprovalDto(
    val id: String,
    val approverName: String,
    val approverRole: String,
    val institutionName: String? = null,
    val approvalDate: String,
    val approvalScope: String,
    val publicDocumentStorageKey: String? = null,
    val documentReferenceNumber: String? = null,
    val status: ApprovalStatus,
)

@Serializable
data class AmaliyahStepDto(
    val id: String,
    val position: Int,
    val stepType: StepType,
    val titleId: String? = null,
    val titleAr: String? = null,
    val arabicText: String? = null,
    val translationId: String? = null,
    val instructionId: String? = null,
    val instructionAr: String? = null,
    val repeatTarget: Int? = null,
    val quranSurahNumber: Int? = null,
    val quranAyahStart: Int? = null,
    val quranAyahEnd: Int? = null,
    val audioGroupId: String? = null,
)
