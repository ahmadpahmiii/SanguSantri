package com.sangusantri.app.data.sync.quran

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sangusantri.app.data.remote.quran.QuranStableVersionConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

/** Performs at most one full update attempt for a newer stable-version target. A handled failure
 * keeps the previous Room snapshot and finishes this work; the scheduler's durable cooldown owns
 * the next attempt so WorkManager cannot repeatedly redownload the large corpus. */
@HiltWorker
class QuranUpdateWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: QuranSyncManager,
    private val syncMetadata: QuranSyncMetadata,
    private val stableVersionConfig: QuranStableVersionConfig,
) : CoroutineWorker(context, params) {
    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override suspend fun doWork(): Result {
        val requestedVersion = inputData.getInt(INPUT_TARGET_VERSION, INVALID_VERSION)
        val targetVersion = maxOf(requestedVersion, stableVersionConfig.getActivatedStableVersion())
        if (targetVersion < QuranStableVersionConfig.BASELINE_VERSION) return Result.success()

        val appliedVersion = syncMetadata.getAppliedStableVersion() ?: return Result.success()
        if (targetVersion <= appliedVersion) return Result.success()

        return try {
            when (syncManager.sync(targetVersion)) {
                is QuranSyncResult.Completed -> Result.success()
                is QuranSyncResult.RetryableFailure,
                is QuranSyncResult.PermanentFailure,
                    -> {
                    syncMetadata.recordFailedUpdateAttempt(targetVersion)
                    Result.success()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            FirebaseCrashlytics.getInstance().recordException(failure)
            syncMetadata.recordFailedUpdateAttempt(targetVersion)
            Result.success()
        }
    }

    companion object {
        const val INPUT_TARGET_VERSION = "target_stable_version"
        private const val INVALID_VERSION = 0
    }
}
