package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import com.sangusantri.app.data.local.entity.TasbihSessionEntity
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession

/** Maps Standalone Tasbih (0.0.2) Room entities to domain models. */

fun TasbihSessionEntity.toDomain(): TasbihSession =
    TasbihSession(
        currentCount = currentCount,
        targetValue = targetValue,
        targetPreset = targetPreset,
        sessionName = sessionName,
        startedAtEpochMillis = startedAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

fun TasbihSession.toEntity(): TasbihSessionEntity =
    TasbihSessionEntity(
        currentCount = currentCount,
        targetValue = targetValue,
        targetPreset = targetPreset,
        sessionName = sessionName,
        startedAtEpochMillis = startedAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

fun TasbihHistoryEntity.toDomain(): TasbihHistoryEntry =
    TasbihHistoryEntry(
        id = id,
        sessionName = sessionName,
        targetValue = targetValue,
        finalCount = finalCount,
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
    )
