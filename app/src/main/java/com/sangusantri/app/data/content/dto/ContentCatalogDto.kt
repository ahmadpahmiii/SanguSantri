package com.sangusantri.app.data.content.dto

import kotlinx.serialization.Serializable

/**
 * The dynamic catalog (`catalog.json`, ADR 0015) — shared verbatim between bundled assets
 * (`app/src/main/assets/content/catalog.json`) and the CMS API's
 * `GET /api/v1/catalog`. Lists every content item's display metadata
 * plus where to fetch its content file; it never carries step data itself.
 */
@Serializable
data class ContentCatalogDto(
    val schemaVersion: Int,
    val items: List<ContentCatalogItemDto>,
)

@Serializable
data class ContentCatalogItemDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val version: Int,
    val contentUrl: String,
    val order: Int,
    val isActive: Boolean,
)
