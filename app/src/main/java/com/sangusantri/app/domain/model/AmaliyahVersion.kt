package com.sangusantri.app.domain.model

/**
 * An immutable, checksum-verified reading of a variant (PRD 10.4, 11.1). Any
 * correction produces a new version; an approved version is never mutated in place.
 */
data class AmaliyahVersion(
    val id: String,
    val variantId: String,
    val versionNumber: Int,
    val schemaVersion: Int,
    val status: AmaliyahVersionStatus,
    val sourceName: String,
    val sourceReference: String,
    val approvalId: String,
    val checksumSha256: String,
    val minimumAppVersionCode: Int,
    val publishedAt: String?,
    val revokedAt: String?,
)
