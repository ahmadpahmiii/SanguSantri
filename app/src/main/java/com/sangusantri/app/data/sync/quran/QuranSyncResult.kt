package com.sangusantri.app.data.sync.quran

/** Outcome of one complete [QuranSyncManager] full-sync attempt (initial preparation or a
 * seven-day refresh — both run the identical fetch/validate/atomic-replace algorithm, QUR-FR-002/004). */
sealed interface QuranSyncResult {
    data object Completed : QuranSyncResult

    data class RetryableFailure(
        val reason: String,
    ) : QuranSyncResult

    data class PermanentFailure(
        val reason: String,
    ) : QuranSyncResult
}
