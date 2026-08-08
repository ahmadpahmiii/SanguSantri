package com.sangusantri.app.data.sync.quran

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Quran full-sync bookkeeping, stored through the existing `app_metadata` key-value table
 * (`docs/engineering/CONTENT_MODEL.md`) rather than a dedicated table solely for two timestamps.
 *
 * The seven-day refresh gate (QUR-FR-004, `docs/product/QURAN_PRD.md` §6.2) is read from the last
 * *successful* full sync only — unlike the amaliyah content sync's "last terminal attempt" gate, a
 * failed Quran refresh does not push the next eligible attempt back, since a refresh is only ever
 * triggered by a user opening the hub, not a periodic background worker.
 */
class QuranSyncMetadata
@Inject
constructor(
    private val appMetadataDao: AppMetadataDao,
) {
    suspend fun getLastSuccessfulSyncAtEpochMillis(): Long? =
        appMetadataDao.getByKey(KEY_LAST_SUCCESSFUL_SYNC)?.updatedAtEpochMillis

    suspend fun recordSuccessfulSync() {
        val now = System.currentTimeMillis()
        appMetadataDao.upsert(AppMetadataEntity(KEY_LAST_SUCCESSFUL_SYNC, "SUCCESS", now))
        appMetadataDao.upsert(AppMetadataEntity(KEY_LAST_SYNC_ATTEMPT, "SUCCESS", now))
    }

    /** Diagnostic only (`docs/operations/PRODUCTION_READINESS.md`) — never read by the refresh
     * staleness gate, which is [getLastSuccessfulSyncAtEpochMillis] alone. */
    suspend fun recordFailedSync() {
        appMetadataDao.upsert(AppMetadataEntity(KEY_LAST_SYNC_ATTEMPT, "FAILED", System.currentTimeMillis()))
    }

    suspend fun isRefreshStale(): Boolean {
        val lastSuccess = getLastSuccessfulSyncAtEpochMillis() ?: return false
        return System.currentTimeMillis() - lastSuccess >= REFRESH_STALE_THRESHOLD_MILLIS
    }

    companion object {
        const val KEY_LAST_SUCCESSFUL_SYNC = "quran_last_full_sync_success"
        const val KEY_LAST_SYNC_ATTEMPT = "quran_last_full_sync_attempt"
        val REFRESH_STALE_THRESHOLD_MILLIS: Long = TimeUnit.DAYS.toMillis(7)
    }
}
