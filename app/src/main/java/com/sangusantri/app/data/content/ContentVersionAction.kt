package com.sangusantri.app.data.content

/**
 * Pure pre-import comparison shared by [com.sangusantri.app.data.local.content.BundledContentBootstrapper]
 * and [com.sangusantri.app.data.sync.ContentSyncManager] so neither has to read a package asset or
 * download a package that [ContentPackageImporter] would reject anyway (sections 5, 12). This is an
 * optimisation only — [ContentPackageImporter.importPackage] always re-runs the same comparison
 * against Room itself before writing anything.
 */
enum class ContentVersionAction {
    IMPORT,
    SKIP_OLDER,
    SKIP_UP_TO_DATE,
    REJECT_CHECKSUM_CONFLICT,
}

fun decideContentVersionAction(
    candidateVersionNumber: Int,
    candidateChecksumSha256: String,
    active: ContentPackageImporter.ActiveVersionSummary?,
): ContentVersionAction =
    when {
        active == null -> ContentVersionAction.IMPORT
        candidateVersionNumber < active.versionNumber -> ContentVersionAction.SKIP_OLDER
        candidateVersionNumber == active.versionNumber ->
            if (candidateChecksumSha256.equals(active.checksumSha256, ignoreCase = true)) {
                ContentVersionAction.SKIP_UP_TO_DATE
            } else {
                ContentVersionAction.REJECT_CHECKSUM_CONFLICT
            }

        else -> ContentVersionAction.IMPORT
    }
