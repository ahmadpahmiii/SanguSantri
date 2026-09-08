package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.entity.ContentStepEntity

/** Maps a validated content detail's step DTOs to their Room entities. */
fun ContentStepDto.toEntity(
    contentId: String,
    position: Int,
): ContentStepEntity =
    ContentStepEntity(
        id = id,
        contentId = contentId,
        position = position,
        arabicText = arabicText,
        translation = translation,
        repeatTarget = repeatTarget,
    )
