package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import javax.inject.Inject

/** Reads and writes reading position via Room, keyed by immutable content version id. */
class ReadingPositionRepositoryImpl
@Inject
constructor(
    private val readingPositionDao: ReadingPositionDao,
) : ReadingPositionRepository {
    override suspend fun getPosition(versionId: String): ReadingPosition? =
        readingPositionDao.getByVersionId(versionId)?.toDomain()

    override suspend fun savePosition(position: ReadingPosition) {
        readingPositionDao.upsert(position.toEntity())
    }
}
