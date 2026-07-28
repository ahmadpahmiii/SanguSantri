package com.sangusantri.app.data.sync

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import javax.inject.Inject

/** Terminal status of the last completed remote content sync attempt (section 4, 15). */
enum class ContentSyncStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
}

/**
 * Sync bookkeeping stored through the existing `app_metadata` key-value table rather than a new
 * table solely for one timestamp (section 4). The 24-hour scheduling gate reads
 * [getLastSyncAtEpochMillis], which only ever advances on a terminal attempt — including a
 * terminal failure — never on an in-flight retry.
 */
class ContentSyncMetadata
@Inject
constructor(
    private val appMetadataDao: AppMetadataDao,
) {
    suspend fun getLastSyncAtEpochMillis(): Long? = appMetadataDao.getByKey(KEY_LAST_SYNC)?.updatedAtEpochMillis

    suspend fun recordTerminalSync(status: ContentSyncStatus) {
        appMetadataDao.upsert(AppMetadataEntity(KEY_LAST_SYNC, status.name, System.currentTimeMillis()))
    }

    companion object {
        const val KEY_LAST_SYNC = "content_last_sync"
    }
}
