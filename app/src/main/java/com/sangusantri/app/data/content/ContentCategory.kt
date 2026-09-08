package com.sangusantri.app.data.content

import com.sangusantri.app.domain.model.Content

/**
 * Which CMS collection an item belongs to, and therefore which route serves it.
 *
 * This replaces the `isSholawat: Boolean` that used to be threaded through the detail fetch. A
 * boolean at a call site says nothing about what `true` means, and getting it backwards asked the
 * wrong category's endpoint — a 404 for content that exists, indistinguishable from an unpublish.
 *
 * [path] is the CMS's own segment and is interpolated into the request URL, so this type existing
 * as a closed set is also what keeps a free-text category off the wire.
 */
enum class ContentCategory(val path: String) {
    AMALIYAH("amaliyah"),
    SHOLAWAT("sholawat"),
    ;

    companion object {
        /**
         * Derived from [Content.isSholawat] rather than comparing the category string here — that
         * predicate already absorbs the transliterations an admin might type ("shalawat",
         * "salawat", …), and duplicating the comparison is how the two would drift apart.
         */
        fun of(content: Content): ContentCategory = if (content.isSholawat) SHOLAWAT else AMALIYAH
    }
}
