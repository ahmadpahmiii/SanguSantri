package com.sangusantri.app.data.local.seed.dto

import kotlinx.serialization.Serializable

/**
 * `content/manifest.json` (PRD 12.2, content-schema.md). Lists every bundled
 * content package and the checksum the importer must verify before parsing it.
 */
@Serializable
data class ContentManifestDto(
    val schemaVersion: Int,
    val generatedAt: String,
    val packages: List<ContentManifestEntryDto>,
)

@Serializable
data class ContentManifestEntryDto(
    val versionId: String,
    val file: String,
    val checksumSha256: String,
)
