package com.sangusantri.app.data.content

/**
 * Per-item result of importing a catalog entry's content file, from either the bundled
 * bootstrapper or remote sync — [ContentImporter] does not distinguish the two (ADR 0015). One
 * malformed or stale item must never affect another.
 */
sealed interface ContentImportOutcome {
    /** No prior Room row existed for this content id; it was inserted fresh. */
    data class Imported(
        val contentId: String,
    ) : ContentImportOutcome

    /** A newer version replaced the previously active content atomically. */
    data class Replaced(
        val contentId: String,
        val oldVersion: Int,
        val newVersion: Int,
    ) : ContentImportOutcome

    /** Same version already active — safe to re-run, no write performed. */
    data class SkippedUpToDate(
        val contentId: String,
    ) : ContentImportOutcome

    /** The catalog's version is older than what Room already has — Room is never downgraded. */
    data class SkippedOlderVersion(
        val contentId: String,
        val localVersion: Int,
    ) : ContentImportOutcome

    /** Rejected before or during import (malformed JSON, structural validation failure,
     * id/version identity mismatch against the catalog, or a database failure that rolled back).
     * No partial write remains. */
    data class Rejected(
        val contentId: String?,
        val reason: String,
    ) : ContentImportOutcome
}
