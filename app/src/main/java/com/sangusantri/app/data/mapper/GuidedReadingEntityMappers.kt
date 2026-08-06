package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.StepProgressEntity
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.StepProgress

/**
 * Maps Guided Reader (Milestone 4) Room entities to domain models — split out of
 * `ContentEntityMappers.kt` to stay under the per-file function-count limit, not because the two
 * are conceptually unrelated to that file's content-hierarchy mappers.
 */

fun GuidedReadingSessionEntity.toDomain(): GuidedReadingSession =
    GuidedReadingSession(
        contentId = contentId,
        currentStepId = currentStepId,
        lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        startedAtEpochMillis = startedAtEpochMillis,
    )

fun GuidedReadingSession.toEntity(): GuidedReadingSessionEntity =
    GuidedReadingSessionEntity(
        contentId = contentId,
        currentStepId = currentStepId,
        lastOpenedAtEpochMillis = lastOpenedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        startedAtEpochMillis = startedAtEpochMillis,
    )

fun StepProgressEntity.toDomain(): StepProgress =
    StepProgress(
        contentId = contentId,
        stepId = stepId,
        currentCount = currentCount,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

fun StepProgress.toEntity(): StepProgressEntity =
    StepProgressEntity(
        contentId = contentId,
        stepId = stepId,
        currentCount = currentCount,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
