package com.sangusantri.app.domain.model

/**
 * Aktivitas (`0.0.3`) root-screen read model — every number derived from real local event
 * timestamps (`ObserveActivityOverviewUseCase`), never fabricated. Each `has*`/`isEntirelyEmpty`
 * property backs this screen's per-section hide-if-empty rule (FR-019-style — a section with
 * nothing real to show renders nothing).
 */
data class ActivityOverview(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val weeklyAmaliyahCompletedCount: Int,
    val weeklyTasbihSessionCount: Int,
    val weeklyTotalMinutes: Long,
    /** Most recent first, capped at 5 — the root screen's preview list. */
    val recentAmaliyahCompletions: List<AmaliyahCompletionEvent>,
    /** Most recent first, capped at 5 — the root screen's preview list. */
    val recentTasbihHistory: List<TasbihHistoryEntry>,
    /** `0.0.4`, Pengingat Amaliyah — soonest-first, capped at 5 — the root screen's preview list. */
    val upcomingReminders: List<Reminder> = emptyList(),
) {
    val hasStreak: Boolean
        get() = currentStreakDays > 0 || longestStreakDays > 0

    val hasWeeklyActivity: Boolean
        get() = weeklyAmaliyahCompletedCount > 0 || weeklyTasbihSessionCount > 0 || weeklyTotalMinutes > 0

    val hasAmaliyahHistory: Boolean
        get() = recentAmaliyahCompletions.isNotEmpty()

    val hasTasbihHistory: Boolean
        get() = recentTasbihHistory.isNotEmpty()

    val hasReminders: Boolean
        get() = upcomingReminders.isNotEmpty()

    /** Screen-level empty state (state 1, "Semua Data Kosong") — the one exception to per-section hiding. */
    val isEntirelyEmpty: Boolean
        get() = !hasStreak && !hasWeeklyActivity && !hasAmaliyahHistory && !hasTasbihHistory && !hasReminders
}
