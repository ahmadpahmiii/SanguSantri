package com.sangusantri.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sangusantri.app.data.remote.RemoteContentFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Remote content sync work (section 11). Transient failures (network error, HTTP 408/429/5xx) get
 * bounded exponential-backoff retries; permanent/data-contract failures record a terminal FAILED
 * status immediately. Either way Room is left untouched on failure and the app never crashes —
 * the next opportunity is the scheduler's own 24-hour gate, not a further WorkManager retry.
 */
@HiltWorker
class ContentSyncWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncCoordinator: ContentSyncCoordinator,
    private val syncMetadata: ContentSyncMetadata,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        when (val outcome = syncCoordinator.sync()) {
            is ContentSyncOutcome.NotModified -> terminal(ContentSyncStatus.NOT_MODIFIED)
            is ContentSyncOutcome.NoChanges -> terminal(ContentSyncStatus.NO_CHANGES, outcome.info)
            is ContentSyncOutcome.Updated -> terminal(ContentSyncStatus.UPDATED, outcome.info)
            is ContentSyncOutcome.PartialFailure -> terminal(ContentSyncStatus.UPDATED, outcome.info)
            is ContentSyncOutcome.CompleteFailure -> terminal(ContentSyncStatus.FAILED, outcome.info)
            is ContentSyncOutcome.Failed -> handleManifestFailure(outcome.failure)
        }

    private suspend fun terminal(
        status: ContentSyncStatus,
        info: ManifestSyncInfo? = null,
    ): Result {
        info?.let { syncMetadata.saveManifestInfo(it) }
        syncMetadata.recordTerminalSync(status)
        return Result.success()
    }

    private suspend fun handleManifestFailure(failure: RemoteContentFailure): Result =
        if (isTransient(failure) && runAttemptCount < MAX_ATTEMPTS - 1) {
            Result.retry()
        } else {
            syncMetadata.recordTerminalSync(ContentSyncStatus.FAILED)
            Result.success()
        }

    private fun isTransient(failure: RemoteContentFailure): Boolean =
        when (failure) {
            RemoteContentFailure.NoConnectivityOrTimeout -> true
            is RemoteContentFailure.HttpStatus ->
                failure.code in TRANSIENT_HTTP_CODES ||
                    failure.code >= HTTP_SERVER_ERROR

            else -> false
        }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val HTTP_SERVER_ERROR = 500
        val TRANSIENT_HTTP_CODES = setOf(408, 429)
    }
}
