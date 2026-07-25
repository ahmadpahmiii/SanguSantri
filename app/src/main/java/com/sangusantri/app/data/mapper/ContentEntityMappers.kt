package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity
import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.AmaliyahVariant
import com.sangusantri.app.domain.model.AmaliyahVersion
import com.sangusantri.app.domain.model.Approval

/** Maps Room entities (data boundary) to plain domain models — the UI must never see entities directly. */

fun AmaliyahEntity.toDomain(): Amaliyah =
    Amaliyah(
        id = id,
        slug = slug,
        titleId = titleId,
        titleAr = titleAr,
        descriptionId = descriptionId,
        descriptionAr = descriptionAr,
        category = category,
    )

fun AmaliyahVariantEntity.toDomain(): AmaliyahVariant =
    AmaliyahVariant(
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

fun ApprovalEntity.toDomain(): Approval =
    Approval(
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

fun AmaliyahVersionEntity.toDomain(): AmaliyahVersion =
    AmaliyahVersion(
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

fun AmaliyahStepEntity.toDomain(): AmaliyahStep =
    AmaliyahStep(
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
