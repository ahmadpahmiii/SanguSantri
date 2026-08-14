package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import javax.inject.Inject

/** Reads and writes reading position via Room, keyed by content id. */
class ReadingPositionRepositoryImpl
@Inject
constructor(
    private val readingPositionDao: ReadingPositionDao,
) : ReadingPositionRepository {
    override suspend fun getPosition(contentId: String): ReadingPosition? =
        readingPositionDao.getByContentId(contentId)?.toDomain()

    override suspend fun getMostRecentPosition(): ReadingPosition? = readingPositionDao.getMostRecent()?.toDomain()

    override suspend fun savePosition(position: ReadingPosition) {
        readingPositionDao.upsert(position.toEntity())
    }
}
