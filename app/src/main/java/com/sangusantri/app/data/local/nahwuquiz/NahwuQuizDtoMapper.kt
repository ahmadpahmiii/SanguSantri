package com.sangusantri.app.data.local.nahwuquiz

import com.sangusantri.app.data.local.entity.NahwuQuizPackageEntity
import com.sangusantri.app.data.local.entity.NahwuQuizQuestionEntity
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizPackageDto
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizQuestionDto
import com.sangusantri.app.domain.model.NahwuQuizOptionKey

/** Maps a validated bundled bank's DTOs to their Room entities. */
fun NahwuQuizPackageDto.toEntity(): NahwuQuizPackageEntity =
    NahwuQuizPackageEntity(
        id = id,
        title = title,
        description = description,
        order = order,
        isActive = isActive,
    )

fun NahwuQuizQuestionDto.toEntity(
    packageId: String,
    position: Int,
): NahwuQuizQuestionEntity =
    NahwuQuizQuestionEntity(
        id = id,
        packageId = packageId,
        order = position,
        stem = stem,
        optionAText = optionA,
        optionBText = optionB,
        optionCText = optionC,
        optionDText = optionD,
        correctOption = NahwuQuizOptionKey.valueOf(correctOption),
        explanation = explanation,
    )
