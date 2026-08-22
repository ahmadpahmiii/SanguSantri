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
         * Canonical [category] value for Sholawat content — the exact string the CMS writes
         * (its `CONTENT_CATEGORIES`, enforced by the `content_category_check` constraint in
         * `cms/db/migrations/007`). Use [Content.isSholawat] to *read*, which also accepts the
         * other transliterations below.
         */
        const val SHOLAWAT_CATEGORY = "Sholawat"

        /**
         * Every spelling of صلوات an admin plausibly types in the CMS, lowercased.
         *
         * The category is free text typed by a human, matched here against a constant compiled
         * into the app — so an exact-equality check turns a one-letter transliteration difference
         * into two silent, confusing failures at once: the item falls through to the Amaliyah
         * surfaces it was supposed to be kept out of, *and* Beranda's Sholawat tile stays disabled
         * because nothing matched. Accepting the known spellings costs a set literal and removes
         * the whole class of bug.
         */
        private val SHOLAWAT_CATEGORY_ALIASES = setOf("shalawat", "sholawat", "salawat", "solawat")
    }

    /**
     * Whether this item belongs to the Sholawat section rather than the Amaliyah ones. Sholawat
     * (0.0.8) deliberately ships its own list and reader, so this is the single predicate that
     * decides both "show it in Sholawat" and "keep it out of Beranda/Jelajahi".
     */
    val isSholawat: Boolean
        get() = category?.trim()?.lowercase() in SHOLAWAT_CATEGORY_ALIASES
}
