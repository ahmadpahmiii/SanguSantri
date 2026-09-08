package com.sangusantri.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.domain.repository.ContentRepository
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
class ContentSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val syncMetadata: ContentSyncMetadata,
) : CoroutineWorker(context, params) {
    /**
     * `Result.success()` on an exhausted retry is deliberate, not a swallowed error: a failed
     * catalogue refresh is not a failed *job*. Room still holds a readable catalogue, the status is
     * recorded for the next scheduling decision, and reporting failure would only make WorkManager
     * back off a job whose own 24-hour gate already governs when it next runs.
     */
    override suspend fun doWork(): Result = when (val result = contentRepository.refreshCatalogue()) {
        is ApiResult.Success -> {
            syncMetadata.recordTerminalSync(ContentSyncStatus.SUCCESS)
            Result.success()
        }

        is ApiResult.Failure ->
            if (result.isRetryable && runAttemptCount < MAX_ATTEMPTS - 1) {
                Result.retry()
            } else {
                syncMetadata.recordTerminalSync(ContentSyncStatus.FAILED)
                Result.success()
            }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
