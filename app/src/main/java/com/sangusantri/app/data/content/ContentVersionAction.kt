package com.sangusantri.app.data.content

/**
 * Pure pre-import comparison shared by [com.sangusantri.app.data.local.content.BundledContentBootstrapper]
 * and [com.sangusantri.app.data.sync.ContentSyncManager] so neither has to read a package asset or
 * download a content file that [ContentImporter] would reject anyway. This is an optimisation
 * only — [ContentImporter.importContentFile] always re-runs the same comparison against Room
 * itself before writing anything. No checksum is involved (ADR 0015) — version is the only
 * signal.
 */
enum class ContentVersionAction {
    IMPORT,
    SKIP_OLDER,
    SKIP_UP_TO_DATE,
}

fun decideContentVersionAction(
    candidateVersion: Int,
    localVersion: Int?,
): ContentVersionAction =
    when {
        localVersion == null -> ContentVersionAction.IMPORT
        candidateVersion > localVersion -> ContentVersionAction.IMPORT
        candidateVersion < localVersion -> ContentVersionAction.SKIP_OLDER
        else -> ContentVersionAction.SKIP_UP_TO_DATE
    }
