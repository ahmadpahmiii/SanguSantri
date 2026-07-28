package com.sangusantri.app.data.content

/**
 * Per-package result of importing a content package, from either the bundled bootstrapper or
 * remote sync — [ContentPackageImporter] does not distinguish the two (PRD 12.4). One malformed
 * or stale package must never affect another.
 */
sealed interface ContentImportOutcome {
    /** No prior Room version existed for this variant; the package was inserted fresh. */
    data class Imported(
        val versionId: String,
    ) : ContentImportOutcome

    /** A newer version replaced the previously active [oldVersionId] atomically. */
    data class Replaced(
        val oldVersionId: String,
        val newVersionId: String,
    ) : ContentImportOutcome

    /** Same version already active with a matching checksum — safe to re-run, no write performed. */
    data class AlreadyUpToDate(
        val versionId: String,
    ) : ContentImportOutcome

    /** The package's version is older than what Room already has — Room is never downgraded. */
    data class SkippedOlderVersion(
        val versionId: String,
        val activeVersionId: String,
    ) : ContentImportOutcome

    /** Same version number as Room's active version, but a different checksum — an immutable-version
     * contract violation. Room is left unchanged. */
    data class ChecksumConflict(
        val versionId: String,
    ) : ContentImportOutcome

    /** Rejected before or during import (checksum mismatch, malformed JSON, structural validation
     * failure, version-identity mismatch, unsupported minimum app version, or a database failure
     * that rolled back). No partial write remains. */
    data class Rejected(
        val versionId: String?,
        val reason: String,
    ) : ContentImportOutcome
}
