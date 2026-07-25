package com.sangusantri.app.data.local.seed

/** Per-package result of a seed import run. One malformed package must never affect another (PRD 12.4). */
sealed interface SeedImportOutcome {
    data class Imported(
        val versionId: String,
    ) : SeedImportOutcome

    data class AlreadyImported(
        val versionId: String,
    ) : SeedImportOutcome

    data class Failed(
        val versionId: String?,
        val reason: String,
    ) : SeedImportOutcome
}
