package com.sangusantri.app.domain.model

import com.sangusantri.app.domain.model.AmalanHarian.Companion.QURAN_PAGE_TARGET
import java.time.LocalDate

/**
 * Aktivitas' daily-consistency read model — the "Amalan Harian" streak
 * (`docs/design/STREAK_GAMIFICATION_CONCEPT.md`).
 *
 * Two targets a day, decided by the product owner on 2026-08-24: one tasbih round and
 * [QURAN_PAGE_TARGET] mushaf pages. **Both** must land for the day to count, and the only
 * forgiveness is udzur ([isUdzurToday]) — there is no weekly grace day. Every value here is derived
 * from real recorded events; nothing is estimated and nothing is backdated.
 */
data class AmalanHarian(
    val dzikirDone: Boolean,
    val quranPagesToday: Int,
    /** While true the Qur'an target is suspended: dzikir alone completes the day. */
    val isUdzurToday: Boolean,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    /** Exactly seven entries, oldest first, the last one being today. */
    val week: List<AmalanDay>,
    /** False until the very first complete day, which is what the first-run copy hangs on. */
    val hasEverCompleted: Boolean,
) {
    /** Suspended during udzur, so a paused target never reads as an unmet one. */
    val quranDone: Boolean
        get() = !isUdzurToday && quranPagesToday >= QURAN_PAGE_TARGET

    /** One during udzur (dzikir only), two otherwise. */
    val targetCount: Int
        get() = if (isUdzurToday) 1 else 2

    val doneCount: Int
        get() = (if (dzikirDone) 1 else 0) + (if (quranDone) 1 else 0)

    val isTodayComplete: Boolean
        get() = dzikirDone && (isUdzurToday || quranPagesToday >= QURAN_PAGE_TARGET)

    /** Pages still owed today, floored at zero — the "tinggal %d halaman lagi" number. */
    val quranPagesLeft: Int
        get() = (QURAN_PAGE_TARGET - quranPagesToday).coerceAtLeast(0)

    /**
     * The highest milestone this streak has reached and not yet been shown for, or `null`.
     *
     * Highest-first on purpose: a streak that passed 3 and 7 while the app was closed shows one
     * sheet for 7, never two in a row — dismissing it marks everything below it as seen.
     */
    fun pendingMilestone(celebrated: Set<Int>): Int? =
        MILESTONES.filter { it <= currentStreakDays }.lastOrNull { it !in celebrated }

    companion object {
        const val QURAN_PAGE_TARGET = 3

        /** Streak lengths worth marking. Ordered so the highest reached one wins. */
        val MILESTONES = listOf(3, 7, 30, 40, 100, 365)
    }
}

/** One day in the seven-day strip. */
data class AmalanDay(
    val date: LocalDate,
    val state: AmalanDayState,
)

enum class AmalanDayState {
    /** Both targets met (or dzikir alone on an udzur day). */
    COMPLETE,

    /** A past day that did not meet its targets — the one state that breaks a streak. */
    INCOMPLETE,

    /** Marked udzur with nothing recorded: bridged, so the streak survives without growing. */
    UDZUR,

    /** Today, still in progress. Never breaks anything. */
    PENDING,
}
