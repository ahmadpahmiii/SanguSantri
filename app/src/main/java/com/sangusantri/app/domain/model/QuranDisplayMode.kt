package com.sangusantri.app.domain.model

/** The single global persisted display choice (QUR-FR-009, §4.1) — there is no separate
 * Halaman/Ayat-mode setting; each display mode selects its own layout automatically. */
enum class QuranDisplayMode {
    ARAB_ONLY,
    ARAB_TRANSLATION,
}
