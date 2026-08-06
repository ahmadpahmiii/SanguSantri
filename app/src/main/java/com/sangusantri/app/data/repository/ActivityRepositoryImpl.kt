package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import com.sangusantri.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityRepositoryImpl
    @Inject
    constructor(
        private val completionEventDao: AmaliyahCompletionEventDao,
    ) : ActivityRepository {
        override suspend fun recordCompletion(
            amaliyahSlug: String,
            amaliyahTitleId: String,
            versionNumber: Int,
            startedAtEpochMillis: Long,
            completedAtEpochMillis: Long,
        ) {
            completionEventDao.insert(
                AmaliyahCompletionEventEntity(
                    amaliyahSlug = amaliyahSlug,
                    amaliyahTitleId = amaliyahTitleId,
                    versionNumber = versionNumber,
                    completedAtEpochMillis = completedAtEpochMillis,
                    durationMillis = (completedAtEpochMillis - startedAtEpochMillis).coerceAtLeast(0L),
                ),
            )
        }

        override fun observeCompletions(): Flow<List<AmaliyahCompletionEvent>> =
            completionEventDao.observeAll().map { list -> list.map { it.toDomain() } }
    }
