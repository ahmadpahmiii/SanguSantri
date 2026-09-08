package com.sangusantri.app.data.content

/**
 * Per-item result of importing one CMS content item. One malformed item must never affect another.
 */
sealed interface ContentWriteOutcome {
    /** No prior Room row existed for this content id; it was inserted fresh. */
    data class Imported(val contentId: String) : ContentWriteOutcome

    /** The item's steps changed and were replaced atomically; the counters are local revisions. */
    data class Replaced(val contentId: String, val oldVersion: Int, val newVersion: Int) : ContentWriteOutcome

    /** Same version already active — safe to re-run, no write performed. */
    data class SkippedUpToDate(val contentId: String) : ContentWriteOutcome

    /** Rejected before or during import (structural validation failure, or a database failure
     * that rolled back). No partial write remains. */
    data class Rejected(val contentId: String?, val reason: String) : ContentWriteOutcome
}
