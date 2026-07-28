package com.sangusantri.app.data.sync

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import javax.inject.Inject

/** Terminal status of the last completed remote content sync attempt (section 10, 11). */
enum class ContentSyncStatus {
    UPDATED,
    NOT_MODIFIED,
    NO_CHANGES,
    FAILED,
}

/**
 * Sync bookkeeping stored through the existing `app_metadata` key-value table rather than a new
 * table solely for one timestamp and one ETag (section 10). The 24-hour scheduling gate reads
 * [getLastSyncAtEpochMillis], which only ever advances on a terminal attempt — including a
 * terminal failure — never on an in-flight retry.
 */
class ContentSyncMetadata
@Inject
constructor(
    private val appMetadataDao: AppMetadataDao,
) {
    suspend fun getStoredEtag(): String? = appMetadataDao.getByKey(KEY_MANIFEST_ETAG)?.value

    suspend fun getLastSyncAtEpochMillis(): Long? = appMetadataDao.getByKey(KEY_LAST_SYNC)?.updatedAtEpochMillis

    suspend fun saveManifestInfo(info: ManifestSyncInfo) {
        info.etag?.let { appMetadataDao.upsert(AppMetadataEntity(KEY_MANIFEST_ETAG, it, now())) }
        appMetadataDao.upsert(AppMetadataEntity(KEY_MANIFEST_VERSION, info.manifestVersion.toString(), now()))
    }

    suspend fun recordTerminalSync(status: ContentSyncStatus) {
        appMetadataDao.upsert(AppMetadataEntity(KEY_LAST_SYNC, status.name, now()))
    }

    private fun now(): Long = System.currentTimeMillis()

    companion object {
        const val KEY_LAST_SYNC = "content_last_sync"
        const val KEY_MANIFEST_ETAG = "content_manifest_etag"
        const val KEY_MANIFEST_VERSION = "content_manifest_version"
    }
}
