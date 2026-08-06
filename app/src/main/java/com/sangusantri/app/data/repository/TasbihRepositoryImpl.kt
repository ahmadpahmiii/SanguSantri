package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.TasbihHistoryDao
import com.sangusantri.app.data.local.dao.TasbihSessionDao
import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TasbihRepositoryImpl
    @Inject
    constructor(
        private val sessionDao: TasbihSessionDao,
        private val historyDao: TasbihHistoryDao,
    ) : TasbihRepository {
        override fun observeSession(): Flow<TasbihSession?> = sessionDao.observe().map { it?.toDomain() }

        override suspend fun incrementCount() {
            val now = System.currentTimeMillis()
            val existing = sessionDao.get()?.toDomain()
            val next =
                when {
                    existing == null ->
                        TasbihSession(
                            currentCount = 1,
                            targetValue = null,
                            targetPreset = TasbihTargetPreset.UNLIMITED,
                            sessionName = null,
                            startedAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        )

                    existing.isTargetReached -> existing.copy(currentCount = 1, updatedAtEpochMillis = now)
                    else -> existing.copy(currentCount = existing.currentCount + 1, updatedAtEpochMillis = now)
                }
            sessionDao.upsert(next.toEntity())
        }

        /**
         * Switching the target selector mid-session (e.g. 33 → 100) must not silently discard whatever
         * was already counted — the same archive-if-nonzero rule [resetSession] uses applies here too,
         * so a session is never lost without a history row, and a genuinely fresh (count = 0) session
         * never creates a spurious empty history entry.
         */
        override suspend fun startSession(
            targetPreset: TasbihTargetPreset,
            targetValue: Int?,
        ) {
            val now = System.currentTimeMillis()
            val existing = sessionDao.get()
            if (existing != null && existing.currentCount > 0) {
                historyDao.insert(
                    TasbihHistoryEntity(
                        sessionName = existing.sessionName,
                        targetValue = existing.targetValue,
                        finalCount = existing.currentCount,
                        startedAtEpochMillis = existing.startedAtEpochMillis,
                        endedAtEpochMillis = now,
                    ),
                )
            }
            sessionDao.upsert(
                TasbihSession(
                    currentCount = 0,
                    targetValue = targetValue,
                    targetPreset = targetPreset,
                    sessionName = existing?.sessionName,
                    startedAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                ).toEntity(),
            )
        }

        override suspend fun renameSession(sessionName: String?) {
            val existing = sessionDao.get()?.toDomain() ?: return
            sessionDao.upsert(
                existing
                    .copy(sessionName = sessionName, updatedAtEpochMillis = System.currentTimeMillis())
                    .toEntity(),
            )
        }

        override suspend fun resetSession() {
            val existing = sessionDao.get()
            if (existing != null && existing.currentCount > 0) {
                historyDao.insert(
                    TasbihHistoryEntity(
                        sessionName = existing.sessionName,
                        targetValue = existing.targetValue,
                        finalCount = existing.currentCount,
                        startedAtEpochMillis = existing.startedAtEpochMillis,
                        endedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
            sessionDao.clear()
        }

        override fun observeHistory(): Flow<List<TasbihHistoryEntry>> =
            historyDao.observeAll().map { list -> list.map { it.toDomain() } }
    }
