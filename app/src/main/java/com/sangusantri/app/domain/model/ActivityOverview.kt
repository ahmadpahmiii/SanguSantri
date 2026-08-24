package com.sangusantri.app.domain.model

/**
 * Aktivitas (`0.0.3`) root-screen read model — every number derived from real local event
 * timestamps (`ObserveActivityOverviewUseCase`), never fabricated. Each `has*`/`isEntirelyEmpty`
 * property backs this screen's per-section hide-if-empty rule (FR-019-style — a section with
 * nothing real to show renders nothing).
 *
 * The streak lives in [AmalanHarian] rather than here: whether a day *counts* is a different
 * question from what happened, with its own two-target rule
 * (`docs/design/STREAK_GAMIFICATION_CONCEPT.md`).
 */
data class ActivityOverview(
    val weeklyAmaliyahCompletedCount: Int,
    val weeklyTasbihSessionCount: Int,
    val weeklyTotalMinutes: Long,
    /** Most recent first, capped at 5 — the root screen's preview list. */
    val recentAmaliyahCompletions: List<AmaliyahCompletionEvent>,
    /** Most recent first, capped at 5 — the root screen's preview list. */
    val recentTasbihHistory: List<TasbihHistoryEntry>,
    /** `0.0.4`, Pengingat Amaliyah — soonest-first, capped at 5 — the root screen's preview list. */
    val upcomingReminders: List<Reminder> = emptyList(),
    /** `0.0.6`, standalone Al-Qur'an Kemenag — most recent first, capped at 5 (QUR-FR-017). */
    val weeklyQuranSessionCount: Int = 0,
    val recentQuranSessions: List<QuranActivityEntry> = emptyList(),
) {
    val hasWeeklyActivity: Boolean
        get() =
            weeklyAmaliyahCompletedCount > 0 ||
                weeklyTasbihSessionCount > 0 ||
                weeklyTotalMinutes > 0 ||
                weeklyQuranSessionCount > 0

    val hasAmaliyahHistory: Boolean
        get() = recentAmaliyahCompletions.isNotEmpty()

    val hasTasbihHistory: Boolean
        get() = recentTasbihHistory.isNotEmpty()

    val hasReminders: Boolean
        get() = upcomingReminders.isNotEmpty()

    val hasQuranHistory: Boolean
        get() = recentQuranSessions.isNotEmpty()

    /**
     * Screen-level empty state (state 1, "Semua Data Kosong") — the one exception to per-section
     * hiding. Amalan Harian sits above this and always renders: a hidden goal card can never start
     * a streak, so it is deliberately not part of this predicate.
     */
    val isEntirelyEmpty: Boolean
        get() =
            !hasWeeklyActivity &&
                !hasAmaliyahHistory &&
                !hasTasbihHistory &&
                !hasReminders &&
                !hasQuranHistory
}
