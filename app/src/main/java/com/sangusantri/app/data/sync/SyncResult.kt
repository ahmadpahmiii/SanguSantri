package com.sangusantri.app.data.sync

/**
 * Result of one [ContentSyncManager.sync] run (section 9). There is no separate partial-failure
 * variant: [Completed] may carry both [Completed.updatedVersionIds] and
 * [Completed.rejectedVersionIds] at once — one package's permanent rejection never fails the whole
 * sync, it is just recorded alongside whichever other packages did update or were skipped.
 */
sealed interface SyncResult {
    data class Completed(
        val updatedVersionIds: List<String>,
        val skippedVersionIds: List<String>,
        val rejectedVersionIds: List<String>,
    ) : SyncResult

    /** Worth retrying the whole sync: network error/timeout, or a manifest/package HTTP
     * 408/429/5xx. Packages already imported before the failure are simply skipped on retry, since
     * Room already matches them. */
    data class RetryableFailure(
        val reason: String,
    ) : SyncResult

    /** Retrying the same manifest response would not help: unsupported schema, empty/malformed
     * manifest body, or a non-retryable manifest HTTP 4xx. */
    data class PermanentFailure(
        val reason: String,
    ) : SyncResult
}
