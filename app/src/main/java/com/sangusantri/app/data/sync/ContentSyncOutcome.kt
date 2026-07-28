package com.sangusantri.app.data.sync

import com.sangusantri.app.data.remote.RemoteContentFailure

/** Manifest-level facts a successful (200 OK) fetch always carries, needed to persist sync state. */
data class ManifestSyncInfo(
    val etag: String?,
    val manifestVersion: Int,
)

/**
 * Result of one [ContentSyncCoordinator.sync] run, distinguishing updated / no-changes /
 * not-modified / partial-failure / complete-failure (section 12) so [ContentSyncWorker] can
 * persist the correct terminal status and decide retry behaviour.
 */
sealed interface ContentSyncOutcome {
    data object NotModified : ContentSyncOutcome

    data class NoChanges(
        val info: ManifestSyncInfo,
    ) : ContentSyncOutcome

    data class Updated(
        val info: ManifestSyncInfo,
        val updatedVersionIds: List<String>,
    ) : ContentSyncOutcome

    /** At least one package updated, at least one other package failed independently (section 12 —
     * per-package failure isolation keeps the successfully imported packages). */
    data class PartialFailure(
        val info: ManifestSyncInfo,
        val updatedVersionIds: List<String>,
        val failedVersionIds: List<String>,
    ) : ContentSyncOutcome

    /** The manifest itself was fetched successfully, but every one of its packages failed. */
    data class CompleteFailure(
        val info: ManifestSyncInfo,
        val failedVersionIds: List<String>,
    ) : ContentSyncOutcome

    /** The manifest fetch itself failed — no package was ever evaluated. */
    data class Failed(
        val failure: RemoteContentFailure,
    ) : ContentSyncOutcome
}
