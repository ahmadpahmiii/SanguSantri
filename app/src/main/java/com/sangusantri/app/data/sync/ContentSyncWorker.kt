package com.sangusantri.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Remote content sync work (section 15). Retryable failures (network error, HTTP 408/429/5xx, or a
 * temporary package download interruption) get bounded exponential-backoff retries; a permanent
 * failure records a terminal FAILED status immediately. Either way Room is left untouched on
 * failure and the app never crashes — the next opportunity is the scheduler's own 24-hour gate, not
 * a further WorkManager retry once attempts are exhausted.
 */
@HiltWorker
class ContentSyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentSyncManager: ContentSyncManager,
    private val syncMetadata: ContentSyncMetadata,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        when (val result = contentSyncManager.sync()) {
            is SyncResult.Completed -> {
                syncMetadata.recordTerminalSync(
                    if (result.rejectedVersionIds.isEmpty()) {
                        ContentSyncStatus.SUCCESS
                    } else {
                        ContentSyncStatus.PARTIAL
                    },
                )
                Result.success()
            }

            is SyncResult.RetryableFailure ->
                if (runAttemptCount < MAX_ATTEMPTS - 1) {
                    Result.retry()
                } else {
                    syncMetadata.recordTerminalSync(ContentSyncStatus.FAILED)
                    Result.success()
                }

            is SyncResult.PermanentFailure -> {
                syncMetadata.recordTerminalSync(ContentSyncStatus.FAILED)
                Result.success()
            }
        }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
