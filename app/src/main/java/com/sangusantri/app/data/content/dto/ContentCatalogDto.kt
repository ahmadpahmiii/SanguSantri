package com.sangusantri.app.data.content.dto

import kotlinx.serialization.Serializable

/**
 * The **bundled-asset** catalog (`app/src/main/assets/content/catalog.json`, `schemaVersion` 1).
 * Lists each bundled item's display metadata plus the local package file holding its steps.
 *
 * Local files only. This shape was once shared verbatim with the CMS API; it is not any more —
 * the CMS speaks `schemaVersion` 3 ([ContentListResponseDto]/[ContentDetailDto]), which has no
 * `version` and no `contentUrl`. The bundled layout is a file convention on disk with no reason to
 * follow the wire, so the two are deliberately separate types.
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
