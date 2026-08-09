package com.sangusantri.app.data.sync.quran

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Durable Quran dataset-version and failed-update bookkeeping in the existing `app_metadata`
 * key-value table. Successful version writes happen inside [QuranSyncManager]'s corpus transaction;
 * this class owns only reads, legacy-baseline adoption, and the failure cooldown gate.
 */
class QuranSyncMetadata
@Inject
constructor(
    private val appMetadataDao: AppMetadataDao,
) {
    suspend fun getAppliedStableVersion(): Int? =
        appMetadataDao.getByKey(KEY_APPLIED_STABLE_VERSION)?.value?.toPositiveIntOrNull()

    suspend fun adoptStableVersion(version: Int): Int {
        require(version > 0)
        getAppliedStableVersion()?.let { return it }
        appMetadataDao.upsert(
            AppMetadataEntity(KEY_APPLIED_STABLE_VERSION, version.toString(), System.currentTimeMillis()),
        )
        return version
    }

    @Suppress("ReturnCount")
    suspend fun isUpdateAttemptEligible(
        appliedVersion: Int,
        targetVersion: Int,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        if (targetVersion <= appliedVersion) return false
        val lastAttempt = appMetadataDao.getByKey(KEY_LAST_FAILED_UPDATE_ATTEMPT) ?: return true
        val lastTarget = lastAttempt.value.toPositiveIntOrNull() ?: return true
        if (lastTarget != targetVersion) return true
        return nowEpochMillis - lastAttempt.updatedAtEpochMillis >= FAILED_UPDATE_COOLDOWN_MILLIS
    }

    suspend fun recordFailedUpdateAttempt(targetVersion: Int) {
        require(targetVersion > 0)
        appMetadataDao.upsert(
            AppMetadataEntity(
                KEY_LAST_FAILED_UPDATE_ATTEMPT,
                targetVersion.toString(),
                System.currentTimeMillis(),
            ),
        )
    }

    private fun String.toPositiveIntOrNull(): Int? = toIntOrNull()?.takeIf { it > 0 }

    companion object {
        const val KEY_APPLIED_STABLE_VERSION = "quran_applied_stable_version"
        const val KEY_LAST_FAILED_UPDATE_ATTEMPT = "quran_last_failed_update_attempt"
        val FAILED_UPDATE_COOLDOWN_MILLIS: Long = TimeUnit.DAYS.toMillis(1)
    }
}
