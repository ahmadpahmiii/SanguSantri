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
 * The two hemistichs of a bait the CMS stored as **one** step, or `null` when this step is not one.
 *
 * A printed qasidah - and NU, which is where the text comes from - writes a whole bait on one line
 * with `۞` between sadr and ajuz. The text therefore already says where the bait divides, so the
 * reader cuts it here at render time instead of the CMS storing two half-steps. That is why Maulid
 * ad-Diba'i imports as 150 rows and not 244: NU translates a bait as one Indonesian sentence, and
 * only 36 of its 94 baits offer a clean place to split that sentence, so splitting on the way in
 * would have guessed wrong 58 times.
 *
 * **Exactly one separator, or this is not a bait.** Shalawat Tarhim has steps carrying two, and a
 * step with two would otherwise be cut into three columns - a form the text does not have.
 * Callers must also honour the item's [com.sangusantri.app.domain.model.ContentLayout]: a `۞` in a
 * stacked item is punctuation the reciter reads past, not an instruction to build columns.
 */
val ContentStep.baitHemistichs: List<String>?
    get() {
        val parts = arabicText.split(BAIT_SEPARATOR)
        if (parts.size != BAIT_HEMISTICHS) return null
        return parts.map { it.trim() }.takeIf { halves -> halves.all { it.isNotEmpty() } }
    }

/**
 * The separator the CMS stores *inside* the step text (U+06DE), which is not the rosette
 * [com.sangusantri.app.feature.sholawat.components] draws between the two rendered columns.
 */
private const val BAIT_SEPARATOR = "۞"

private const val BAIT_HEMISTICHS = 2

/**
 * Whether these steps support Panduan (guided) mode at all. Guided mode *is* the tasbih counter
 * walk-through, so content where nothing is counted — Sholawat, a plain doa — has no guided mode
 * to offer, and the reading-mode gate, the mode-switch pill and the overflow entry all disappear
 * together. One predicate so those four surfaces can never disagree.
 */
fun List<ContentStep>.hasGuidedMode(): Boolean = any { it.effectiveRepeatTarget != null }
