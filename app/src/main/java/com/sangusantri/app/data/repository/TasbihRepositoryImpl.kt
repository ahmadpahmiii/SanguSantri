package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.TasbihHistoryDao
import com.sangusantri.app.data.local.dao.TasbihSessionDao
import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import com.sangusantri.app.data.local.entity.TasbihSessionEntity
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TasbihRepositoryImpl @Inject constructor(
    private val sessionDao: TasbihSessionDao,
    private val historyDao: TasbihHistoryDao,
) : TasbihRepository {
    override fun observeSession(): Flow<TasbihSession?> = sessionDao.observe().map { it?.toDomain() }

    /**
     * Reaching the target *is* finishing — so the round is archived to history right there,
     * not later when something else happens to trigger a reset. Counting 33 of 33 and then
     * finding an empty Riwayat was the whole confusion, and tapping once more used to restart
     * the count over the top of a round that had never been recorded at all.
     */
    override suspend fun incrementCount() {
        val now = System.currentTimeMillis()
        val existing = sessionDao.get()?.toDomain()
        // Belt and braces for a round that reached its target before this rule existed (or in a
        // build that crashed between the two writes below) — the dedupe makes it a no-op otherwise.
        if (existing != null && existing.isTargetReached) archiveIfUnrecorded(existing.toEntity(), now)
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

                // A new round, not a continuation: its own start time, so its own history row.
                existing.isTargetReached ->
                    existing.copy(
                        currentCount = 1,
                        startedAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    )

                else -> existing.copy(currentCount = existing.currentCount + 1, updatedAtEpochMillis = now)
            }
        val entity = next.toEntity()
        sessionDao.upsert(entity)
        if (next.isTargetReached) archiveIfUnrecorded(entity, now)
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
        if (existing != null) archiveIfUnrecorded(existing, now)
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
        if (existing != null) archiveIfUnrecorded(existing, System.currentTimeMillis())
        sessionDao.clear()
    }

    override fun observeHistory(): Flow<List<TasbihHistoryEntry>> =
        historyDao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * The single place a session becomes a history row. Nothing counted (count = 0) is not a
     * session, and a round already filed when it hit its target is not filed twice — the start
     * timestamp identifies the round ([TasbihHistoryDao.isArchived]).
     */
    private suspend fun archiveIfUnrecorded(
        session: TasbihSessionEntity,
        endedAtEpochMillis: Long,
    ) {
        if (session.currentCount == 0) return
        if (historyDao.isArchived(session.startedAtEpochMillis)) return
        historyDao.insert(
            TasbihHistoryEntity(
                sessionName = session.sessionName,
                targetValue = session.targetValue,
                finalCount = session.currentCount,
                startedAtEpochMillis = session.startedAtEpochMillis,
                endedAtEpochMillis = endedAtEpochMillis,
            ),
        )
    }
}
