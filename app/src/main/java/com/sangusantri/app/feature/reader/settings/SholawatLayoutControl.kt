package com.sangusantri.app.feature.reader.settings

/**
 * Bundles the Sholawat-reader-only layout controls so [ReaderSettingsSheet] stays under the
 * shared-parameter-count limit while still being reused (not duplicated) by a third reader —
 * `null` when opened from the Full or Guided Reader, neither of which has a bait concept.
 *
 * [pairingAvailable] is the CMS's `layout` flag for the open item, not a user preference. When the
 * CMS calls an item continuous prose, the two-column switch is shown disabled rather than hidden:
 * hiding it would read as "this app cannot do two columns", while a disabled switch with its
 * caption says "not this item". Pairing prose would split its sentences across columns, so the
 * user preference can only ever narrow what the CMS allows, never widen it.
 */
data class SholawatLayoutControl(
    val pairingAvailable: Boolean,
    val twoColumn: Boolean,
    val onTwoColumnChange: (Boolean) -> Unit,
    val baitGap: Boolean,
    val onBaitGapChange: (Boolean) -> Unit,
)
