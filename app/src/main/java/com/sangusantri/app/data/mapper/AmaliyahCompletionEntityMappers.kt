package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent

/** Maps Aktivitas (0.0.3) completion-event Room entities to domain models. */
fun AmaliyahCompletionEventEntity.toDomain(): AmaliyahCompletionEvent =
    AmaliyahCompletionEvent(
        id = id,
        amaliyahSlug = amaliyahSlug,
        amaliyahTitleId = amaliyahTitleId,
        versionNumber = versionNumber,
        completedAtEpochMillis = completedAtEpochMillis,
        durationMillis = durationMillis,
    )
