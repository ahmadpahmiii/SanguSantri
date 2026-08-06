package com.sangusantri.app.domain.model

/**
 * One ordered reading step within a [Content] item (ADR 0015). Renders identically for full
 * reading mode and guided reading mode — the two reader modes must never fork this model.
 * Every step carries Arabic text, a translation, and a repeat target; there is no step "type."
 */
data class ContentStep(
    val id: String,
    val contentId: String,
    val position: Int,
    val arabicText: String,
    val translation: String,
    val repeatTarget: Int,
)
