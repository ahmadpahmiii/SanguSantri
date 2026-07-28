package com.sangusantri.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * `GET /v1/content/manifest` response body (section 7.1). Transport-specific to the backend —
 * deliberately a different shape from [com.sangusantri.app.data.local.content.BundledManifestDto]
 * (status/minimum-app-version fields the bundled manifest has no need for), sharing only the
 * underlying [com.sangusantri.app.data.content.dto.ContentPackageDto] package contract.
 */
@Serializable
data class RemoteContentManifestDto(
    val manifestVersion: Int,
    val schemaVersion: Int,
    val generatedAt: String,
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
    val status: String,
)
