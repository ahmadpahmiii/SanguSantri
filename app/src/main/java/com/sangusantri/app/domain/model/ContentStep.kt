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
     * The step's counter target, or `null` when there is nothing to count. The CMS stores "no
     * counter" as SQL NULL, a stale row or hand-edited bundled asset can carry 0 or a negative,
     * and a target of **1** is a step read once — none of them is a repetition, so all of them
     * read as `null` here rather than putting a one-tap tasbih (and a "Target 1 kali" label, and a
     * disabled "Lanjut") in front of the reader. Every read goes through this property rather than
     * comparing `repeatTarget` directly, so the counter, the status label, the Full Reader's
     * repetition shortcut and the continue gate can never disagree.
     */
    val effectiveRepeatTarget: Int?
        get() = repeatTarget?.takeIf { it > 1 }
}

/**
 * Whether these steps support Panduan (guided) mode at all. Guided mode *is* the tasbih counter
 * walk-through, so content where nothing is counted — Sholawat, a plain doa — has no guided mode
 * to offer, and the reading-mode gate, the mode-switch pill and the overflow entry all disappear
 * together. One predicate so those four surfaces can never disagree.
 */
fun List<ContentStep>.hasGuidedMode(): Boolean = any { it.effectiveRepeatTarget != null }
