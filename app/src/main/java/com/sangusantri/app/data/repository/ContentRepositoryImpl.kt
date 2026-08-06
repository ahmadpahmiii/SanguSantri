package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Reads the content catalogue from Room, the local source of truth (PRD 12.1). */
class ContentRepositoryImpl
    @Inject
    constructor(
        private val contentDao: ContentDao,
        private val contentStepDao: ContentStepDao,
    ) : ContentRepository {
        override fun observeActiveContent(): Flow<List<Content>> =
            contentDao.observeActive().map { list -> list.map { it.toDomain() } }

        override suspend fun getContentById(contentId: String): Content? = contentDao.getById(contentId)?.toDomain()

        override suspend fun getContentDetail(contentId: String): ContentDetail? {
            val content = contentDao.getById(contentId) ?: return null
            return ContentDetail(
                content = content.toDomain(),
                steps = contentStepDao.getByContentId(contentId).map { it.toDomain() },
            )
        }
    }
