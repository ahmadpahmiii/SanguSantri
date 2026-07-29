package com.sangusantri.app.domain.model

/**
 * One recorded Guided Reader completion (Aktivitas, `0.0.3`) — written exactly once per valid
 * completion action (FR-007). [amaliyahTitleId] and [versionNumber] are snapshots taken at
 * completion time, not live references, so this history entry never changes if the amaliyah is
 * later renamed or its content package updated (Content Delivery Foundation, ADR 0012, deletes
 * version-scoped progress on replacement — this table is deliberately decoupled from that).
 * [durationMillis] is a real snapshot (`completedAtEpochMillis - startedAtEpochMillis`), never
 * fabricated.
 */
data class AmaliyahCompletionEvent(
    val id: Long,
    val amaliyahSlug: String,
    val amaliyahTitleId: String,
    val versionNumber: Int,
    val completedAtEpochMillis: Long,
    val durationMillis: Long,
)
