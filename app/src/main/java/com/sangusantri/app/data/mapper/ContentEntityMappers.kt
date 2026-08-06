package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReadingPosition

/** Maps Room entities (data boundary) to plain domain models — the UI must never see entities directly. */

fun ContentEntity.toDomain(): Content =
    Content(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        category = category,
        version = version,
        order = order,
        isActive = isActive,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
    )

fun ContentStepEntity.toDomain(): ContentStep =
    ContentStep(
        id = id,
        contentId = contentId,
        position = position,
        arabicText = arabicText,
        translation = translation,
        repeatTarget = repeatTarget,
    )

fun ReadingPositionEntity.toDomain(): ReadingPosition =
    ReadingPosition(
        contentId = contentId,
        itemIndex = itemIndex,
        itemOffset = itemOffset,
        lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    )

fun ReadingPosition.toEntity(): ReadingPositionEntity =
    ReadingPositionEntity(
        contentId = contentId,
        itemIndex = itemIndex,
        itemOffset = itemOffset,
        lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
    )
