package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    override fun observeContentIdsMatchingStepText(query: String): Flow<List<String>> {
        val needle = query.trim()
        return if (needle.isEmpty()) {
            flowOf(emptyList())
        } else {
            contentStepDao.observeContentIdsMatchingText(needle.escapeLikeWildcards())
        }
    }

    override suspend fun getContentById(contentId: String): Content? = contentDao.getById(contentId)?.toDomain()

    override suspend fun getContentDetail(contentId: String): ContentDetail? {
        val content = contentDao.getById(contentId) ?: return null
        return ContentDetail(
            content = content.toDomain(),
            steps = contentStepDao.getByContentId(contentId).map { it.toDomain() },
        )
    }

    /** `%`, `_` and the escape character itself are literal text when typed into a search box. */
    private fun String.escapeLikeWildcards(): String = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}
