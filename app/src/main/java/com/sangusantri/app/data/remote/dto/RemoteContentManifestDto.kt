package com.sangusantri.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /v1/content/manifest` response body (section 10). Transport-specific to the backend —
 * deliberately a different shape from [com.sangusantri.app.data.local.content.BundledManifestDto]
 * (`minimumAppVersionCode` has no bundled equivalent), sharing only the underlying
 * [com.sangusantri.app.data.content.dto.ContentPackageDto] package contract. The manifest lists
 * only each variant's currently active published package — the backend keeps full immutable
 * revision history itself, never sent to Android.
 */
@Serializable
data class RemoteContentManifestDto(
    val schemaVersion: Int,
    val packages: List<RemoteContentManifestPackageDto>,
)

@Serializable
data class RemoteContentManifestPackageDto(
    val contentId: String,
    val variantId: String,
    val versionId: String,
    val versionNumber: Int,
    val checksumSha256: String,
    val minimumAppVersionCode: Int,
)
