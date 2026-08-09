package com.sangusantri.app.data.sync.quran

/** Outcome of one complete [QuranSyncManager] attempt (initial preparation or a Remote Config
 * version-triggered update; both use the same validate-and-atomically-replace algorithm). */
sealed interface QuranSyncResult {
    data object Completed : QuranSyncResult

    data class RetryableFailure(
        val reason: String,
    ) : QuranSyncResult

    data class PermanentFailure(
        val reason: String,
    ) : QuranSyncResult
}
