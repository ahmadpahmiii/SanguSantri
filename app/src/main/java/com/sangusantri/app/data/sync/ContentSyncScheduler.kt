package com.sangusantri.app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Opportunistic one-time sync trigger (section 10), gated by the last *terminal* remote sync
 * attempt — including a terminal failure, so a backend that is still being deployed is not
 * hammered every app launch. Not a periodic worker: [enqueueIfStale] is called from app startup
 * or foreground entry only.
 */
class ContentSyncScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val syncMetadata: ContentSyncMetadata,
    ) {
        suspend fun enqueueIfStale() {
            val lastSyncAt = syncMetadata.getLastSyncAtEpochMillis()
            val isStale = lastSyncAt == null || System.currentTimeMillis() - lastSyncAt >= STALE_THRESHOLD_MILLIS
            if (!isStale) return

            val request =
                OneTimeWorkRequestBuilder<ContentSyncWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    ).build()

            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        companion object {
            const val UNIQUE_WORK_NAME = "sangu-santri-content-sync"
            private val STALE_THRESHOLD_MILLIS = TimeUnit.HOURS.toMillis(24)
        }
    }
