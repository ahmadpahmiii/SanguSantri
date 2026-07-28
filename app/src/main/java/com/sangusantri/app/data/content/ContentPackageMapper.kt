package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.AmaliyahDto
import com.sangusantri.app.data.content.dto.AmaliyahStepDto
import com.sangusantri.app.data.content.dto.AmaliyahVariantDto
import com.sangusantri.app.data.content.dto.AmaliyahVersionDto
import com.sangusantri.app.data.content.dto.ApprovalDto
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity

/** Maps a validated content package's DTOs (PRD 12.2 boundary) to their Room entities. */

fun AmaliyahDto.toEntity(): AmaliyahEntity =
    AmaliyahEntity(
        id = id,
        slug = slug,
        titleId = titleId,
        titleAr = titleAr,
        descriptionId = descriptionId,
        descriptionAr = descriptionAr,
        category = category,
    )

fun AmaliyahVariantDto.toEntity(amaliyahId: String): AmaliyahVariantEntity =
    AmaliyahVariantEntity(
        id = id,
        amaliyahId = amaliyahId,
        slug = slug,
        nameId = nameId,
        nameAr = nameAr,
        ownerType = ownerType,
        pondokId = pondokId,
        visibility = visibility,
        isDefault = isDefault,
    )

fun ApprovalDto.toEntity(): ApprovalEntity =
    ApprovalEntity(
        id = id,
        approverName = approverName,
        approverRole = approverRole,
        institutionName = institutionName,
        approvalDate = approvalDate,
        approvalScope = approvalScope,
        publicDocumentStorageKey = publicDocumentStorageKey,
        documentReferenceNumber = documentReferenceNumber,
        status = status,
    )

fun AmaliyahVersionDto.toEntity(
    variantId: String,
    schemaVersion: Int,
    approvalId: String,
    checksumSha256: String,
): AmaliyahVersionEntity =
    AmaliyahVersionEntity(
        id = id,
        variantId = variantId,
        versionNumber = versionNumber,
        schemaVersion = schemaVersion,
        status = status,
        sourceName = sourceName,
        sourceReference = sourceReference,
        approvalId = approvalId,
        checksumSha256 = checksumSha256,
        minimumAppVersionCode = minimumAppVersionCode,
        publishedAt = publishedAt,
        revokedAt = revokedAt,
    )

fun AmaliyahStepDto.toEntity(versionId: String): AmaliyahStepEntity =
    AmaliyahStepEntity(
        id = id,
        versionId = versionId,
        position = position,
        stepType = stepType,
        titleId = titleId,
        titleAr = titleAr,
        arabicText = arabicText,
        translationId = translationId,
        instructionId = instructionId,
        instructionAr = instructionAr,
        repeatTarget = repeatTarget,
        quranSurahNumber = quranSurahNumber,
        quranAyahStart = quranAyahStart,
        quranAyahEnd = quranAyahEnd,
        audioGroupId = audioGroupId,
    )
