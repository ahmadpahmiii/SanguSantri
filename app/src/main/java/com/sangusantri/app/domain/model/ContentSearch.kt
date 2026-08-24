package com.sangusantri.app.domain.model

/**
 * One search rule for both catalogue surfaces (Jelajahi and Sholawat), so "cari judul atau ayat"
 * means the same thing on each.
 *
 * Title, description and category are matched here; [stepMatchedIds] carries the per-ayah matches
 * Room found in `content_steps` (`ContentRepository.observeContentIdsMatchingStepText`), which this
 * cannot do in memory because step text is not part of [Content].
 *
 * A blank [query] matches everything — an empty search box is not a filter.
 */
fun Content.matchesSearch(
    query: String,
    stepMatchedIds: Set<String>,
): Boolean {
    val needle = query.trim()
    return needle.isEmpty() ||
        title.contains(needle, ignoreCase = true) ||
        description.contains(needle, ignoreCase = true) ||
        category?.contains(needle, ignoreCase = true) == true ||
        id in stepMatchedIds
}
