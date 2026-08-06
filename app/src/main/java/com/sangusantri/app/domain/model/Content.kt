package com.sangusantri.app.domain.model

/**
 * One catalog item — a public or pesantren-specific amaliyah, or any future content type
 * published through the dynamic catalog (ADR 0015). Replaces the former
 * Amaliyah/AmaliyahVariant/AmaliyahVersion hierarchy with one flat, catalog-driven row.
 */
data class Content(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val category: String?,
    val version: Int,
    val order: Int,
    val isActive: Boolean,
    val sourceName: String,
    val sourceUrl: String,
)
