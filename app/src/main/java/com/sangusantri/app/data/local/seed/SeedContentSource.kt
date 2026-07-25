package com.sangusantri.app.data.local.seed

/** Where the seed importer reads the manifest and content packages from (PRD 12.2). */
interface SeedContentSource {
    fun readManifest(): ByteArray

    fun readPackage(fileName: String): ByteArray
}
