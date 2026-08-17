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
) {
    companion object {
        /**
         * [category] value identifying Sholawat content (`docs/engineering/CONTENT_MODEL.md`'s
         * category taxonomy). Kept out of the generic Amaliyah surfaces (Beranda's featured
         * section, Jelajahi Amaliyah) — read only by `feature/sholawat`'s own list screen — since
         * Sholawat `0.0.8` deliberately ships with its own dedicated reader, not the Full/Guided
         * Amaliyah reader.
         */
        const val SHOLAWAT_CATEGORY = "Shalawat"
    }
}
