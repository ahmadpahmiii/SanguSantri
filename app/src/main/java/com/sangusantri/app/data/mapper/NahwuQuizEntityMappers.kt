package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.dao.NahwuQuizPackageSummaryRow
import com.sangusantri.app.data.local.entity.NahwuQuizAttemptEntity
import com.sangusantri.app.data.local.entity.NahwuQuizPackageEntity
import com.sangusantri.app.data.local.entity.NahwuQuizQuestionEntity
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizOption
import com.sangusantri.app.domain.model.NahwuQuizOptionKey
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizQuestion

fun NahwuQuizPackageEntity.toDomain(questionCount: Int): NahwuQuizPackage =
    NahwuQuizPackage(
        id = id,
        title = title,
        description = description,
        order = order,
        isActive = isActive,
        questionCount = questionCount,
    )

fun NahwuQuizPackageSummaryRow.toDomain(): NahwuQuizPackage =
    NahwuQuizPackage(
        id = id,
        title = title,
        description = description,
        order = order,
        isActive = isActive,
        questionCount = questionCount,
    )

fun NahwuQuizQuestionEntity.toDomain(): NahwuQuizQuestion =
    NahwuQuizQuestion(
        id = id,
        packageId = packageId,
        order = order,
        stem = stem,
        options =
            listOf(
                NahwuQuizOption(NahwuQuizOptionKey.A, optionAText),
                NahwuQuizOption(NahwuQuizOptionKey.B, optionBText),
                NahwuQuizOption(NahwuQuizOptionKey.C, optionCText),
                NahwuQuizOption(NahwuQuizOptionKey.D, optionDText),
            ),
        correctOption = correctOption,
        explanation = explanation,
    )

fun NahwuQuizAttemptEntity.toDomain(): NahwuQuizAttempt =
    NahwuQuizAttempt(
        id = id,
        packageId = packageId,
        startedAtEpochMillis = startedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        currentQuestionIndex = currentQuestionIndex,
        correctCount = correctCount,
        totalCount = totalCount,
    )
