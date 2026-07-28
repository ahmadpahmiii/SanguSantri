package com.sangusantri.app.data.local.content

import kotlinx.serialization.Serializable

/**
 * `content/manifest.json` (PRD 12.2, content-schema.md). Lists every bundled content package,
 * the Room variant/version it belongs to, and the checksum [BundledContentBootstrapper] must
 * verify before parsing it. `variantId`/`versionNumber` let the bootstrapper compare against
 * Room's active version *before* reading the package asset (section 5) — an older or
 * already-current bundled entry is skipped without ever opening its file. Transport-specific to
 * bundled assets — the backend's manifest is
 * [com.sangusantri.app.data.remote.dto.RemoteContentManifestDto], a deliberately different shape
 * (`minimumAppVersionCode` has no bundled equivalent) sharing only the underlying
 * [com.sangusantri.app.data.content.dto.ContentPackageDto].
 */
@Serializable
data class BundledManifestDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val packages: List<BundledManifestEntryDto>,
)

@Serializable
data class BundledManifestEntryDto(
    val variantId: String,
    val versionId: String,
    val versionNumber: Int,
    val file: String,
    val checksumSha256: String,
)
