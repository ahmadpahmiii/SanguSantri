package com.sangusantri.app.data.content

/**
 * Per-item result of importing one CMS content item. One malformed item must never affect another.
 */
sealed interface ContentImportOutcome {
    /** No prior Room row existed for this content id; it was inserted fresh. */
    data class Imported(
        val contentId: String,
    ) : ContentImportOutcome

    /** The item's steps changed and were replaced atomically; the counters are local revisions. */
    data class Replaced(
        val contentId: String,
        val oldVersion: Int,
        val newVersion: Int,
    ) : ContentImportOutcome

    /** Same version already active — safe to re-run, no write performed. */
    data class SkippedUpToDate(
        val contentId: String,
    ) : ContentImportOutcome

    /** Rejected before or during import (structural validation failure, or a database failure
     * that rolled back). No partial write remains. */
    data class Rejected(
        val contentId: String?,
        val reason: String,
    ) : ContentImportOutcome
}
