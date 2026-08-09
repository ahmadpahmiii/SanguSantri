package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.GuidedReadingSessionDao
import com.sangusantri.app.data.local.dao.StepProgressDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.StepProgress
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import javax.inject.Inject

class GuidedReadingRepositoryImpl
    @Inject
    constructor(
        private val sessionDao: GuidedReadingSessionDao,
        private val stepProgressDao: StepProgressDao,
    ) : GuidedReadingRepository {
        override suspend fun getSession(contentId: String): GuidedReadingSession? =
            sessionDao.getByContentId(contentId)?.toDomain()

    override suspend fun getMostRecentIncompleteSession(): GuidedReadingSession? =
        sessionDao.getMostRecentIncomplete()?.toDomain()

        override suspend fun saveSession(session: GuidedReadingSession) {
            sessionDao.upsert(session.toEntity())
        }

        override suspend fun getStepProgress(contentId: String): List<StepProgress> =
            stepProgressDao.getByContentId(contentId).map { it.toDomain() }

        override suspend fun saveStepProgress(progress: StepProgress) {
            stepProgressDao.upsert(progress.toEntity())
        }
    }
