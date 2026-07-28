package com.sangusantri.app.data.local.content

import kotlinx.serialization.Serializable

/**
 * `content/manifest.json` (PRD 12.2, content-schema.md). Lists every bundled content package and
 * the checksum [BundledContentBootstrapper] must verify before parsing it. Transport-specific to
 * bundled assets — the backend's manifest is [com.sangusantri.app.data.remote.dto.RemoteContentManifestDto],
 * a deliberately different shape (PRD 7.1's ETag/status/minimum-version fields have no bundled
 * equivalent) sharing only the underlying [com.sangusantri.app.data.content.dto.ContentPackageDto].
 */
@Serializable
data class BundledManifestDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val packages: List<BundledManifestEntryDto>,
)

@Serializable
data class BundledManifestEntryDto(
    val versionId: String,
    val file: String,
    val checksumSha256: String,
)
