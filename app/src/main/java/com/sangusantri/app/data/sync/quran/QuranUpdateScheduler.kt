package com.sangusantri.app.data.sync.quran

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sangusantri.app.data.local.quran.QuranLocalDataset
import com.sangusantri.app.data.remote.quran.QuranStableVersionConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Checks the lightweight Remote Config version and enqueues one corpus replacement only when its
 * monotonic target is newer than the complete local dataset. There is deliberately no periodic or
 * calendar-based Quran sync. */
class QuranUpdateScheduler
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val localDataset: QuranLocalDataset,
    private val syncMetadata: QuranSyncMetadata,
    private val stableVersionConfig: QuranStableVersionConfig,
) {
    suspend fun enqueueIfUpdateAvailable() {
        if (!localDataset.isComplete()) return

        val localVersion =
            syncMetadata.getAppliedStableVersion()
                ?: syncMetadata.adoptStableVersion(QuranStableVersionConfig.BASELINE_VERSION)
        val targetVersion = stableVersionConfig.fetchStableVersion()
        if (!syncMetadata.isUpdateAttemptEligible(localVersion, targetVersion)) return

        val request =
            OneTimeWorkRequestBuilder<QuranUpdateWorker>()
                .setInputData(workDataOf(QuranUpdateWorker.INPUT_TARGET_VERSION to targetVersion))
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .setRequiresBatteryNotLow(true)
                        .build(),
                ).build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "sangu-santri-quran-version-update"
    }
}
