package com.sangusantri.app.domain.model

/**
 * One ordered reading step within a [Content] item (ADR 0015). Renders identically for full
 * reading mode and guided reading mode — the two reader modes must never fork this model.
 * Every step carries Arabic text and a translation; the repeat target is optional.
 */
data class ContentStep(
    val id: String,
    val contentId: String,
    val position: Int,
    val arabicText: String,
    val translation: String,
    /**
     * `null` = this step has no repetition count, so no tasbih counter is shown for it. When no
     * step in an item has one, the whole item has no Panduan (guided) mode — see [hasGuidedMode].
     */
    val repeatTarget: Int?,
) {
    /**
     * The step's counter target, or `null` when it has none. The CMS stores "no counter" as SQL
     * NULL, but a stale row or hand-edited bundled asset can still carry 0 or a negative, and both
     * mean the same thing to a reader — so every read goes through here rather than comparing
     * `repeatTarget` directly.
     */
    val effectiveRepeatTarget: Int?
        get() = repeatTarget?.takeIf { it > 0 }
}

/**
 * Whether these steps support Panduan (guided) mode at all. Guided mode *is* the tasbih counter
 * walk-through, so content where nothing is counted — Sholawat, a plain doa — has no guided mode
 * to offer, and the reading-mode gate, the mode-switch pill and the overflow entry all disappear
 * together. One predicate so those four surfaces can never disagree.
 */
fun List<ContentStep>.hasGuidedMode(): Boolean = any { it.effectiveRepeatTarget != null }
