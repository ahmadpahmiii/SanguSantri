package com.sangusantri.app.domain.usecase

import com.sangusantri.app.domain.model.ActivityOverview
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.repository.ActivityRepository
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Combines [ActivityRepository] (amaliyah completions) and [TasbihRepository] (tasbih history,
 * reused directly rather than duplicated — `docs/engineering/ARCHITECTURE.md`'s per-concern-
 * repository convention) into the read model Aktivitas (`0.0.3`) needs: streak, this-week summary,
 * and the recent-5 preview lists. Combining two repositories with genuine aggregation logic
 * (streak/weekly-window math) is exactly when `CODING_STANDARD.md` says a use case is warranted.
 */
class ObserveActivityOverviewUseCase
    @Inject
    constructor(
        private val activityRepository: ActivityRepository,
        private val tasbihRepository: TasbihRepository,
    ) {
        operator fun invoke(zoneId: ZoneId = ZoneId.systemDefault()): Flow<ActivityOverview> =
            combine(
                activityRepository.observeCompletions(),
                tasbihRepository.observeHistory(),
            ) { completions, tasbihHistory ->
                buildOverview(completions, tasbihHistory, zoneId)
            }

        private fun buildOverview(
            completions: List<AmaliyahCompletionEvent>,
            tasbihHistory: List<TasbihHistoryEntry>,
            zoneId: ZoneId,
        ): ActivityOverview {
            val now = System.currentTimeMillis()
            val activeDates =
                (completions.map { it.completedAtEpochMillis } + tasbihHistory.map { it.endedAtEpochMillis })
                    .mapTo(mutableSetOf()) { epochMillisToLocalDate(it, zoneId) }

            val weekStart = now - MILLIS_PER_WEEK
            val weeklyCompletions = completions.filter { it.completedAtEpochMillis >= weekStart }
            val weeklyTasbih = tasbihHistory.filter { it.endedAtEpochMillis >= weekStart }
            val weeklyDurationMillis =
                weeklyCompletions.sumOf { it.durationMillis } +
                    weeklyTasbih.sumOf { it.endedAtEpochMillis - it.startedAtEpochMillis }

            return ActivityOverview(
                currentStreakDays = calculateCurrentStreak(activeDates, epochMillisToLocalDate(now, zoneId)),
                longestStreakDays = calculateLongestStreak(activeDates),
                weeklyAmaliyahCompletedCount = weeklyCompletions.size,
                weeklyTasbihSessionCount = weeklyTasbih.size,
                weeklyTotalMinutes = weeklyDurationMillis / MILLIS_PER_MINUTE,
                recentAmaliyahCompletions = completions.take(RECENT_LIMIT),
                recentTasbihHistory = tasbihHistory.take(RECENT_LIMIT),
            )
        }

        private fun epochMillisToLocalDate(
            epochMillis: Long,
            zoneId: ZoneId,
        ): LocalDate = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()

        /** Counts backward from today (or yesterday, if nothing happened yet today) through consecutive active days. */
        private fun calculateCurrentStreak(
            activeDates: Set<LocalDate>,
            today: LocalDate,
        ): Int {
            var cursor = if (activeDates.contains(today)) today else today.minusDays(1)
            if (!activeDates.contains(cursor)) return 0
            var streak = 0
            while (activeDates.contains(cursor)) {
                streak++
                cursor = cursor.minusDays(1)
            }
            return streak
        }

        private fun calculateLongestStreak(activeDates: Set<LocalDate>): Int {
            if (activeDates.isEmpty()) return 0
            val sortedDates = activeDates.sorted()
            var longest = 1
            var current = 1
            for (index in 1 until sortedDates.size) {
                current = if (sortedDates[index] == sortedDates[index - 1].plusDays(1)) current + 1 else 1
                longest = maxOf(longest, current)
            }
            return longest
        }

        private companion object {
            const val RECENT_LIMIT = 5
            const val MILLIS_PER_MINUTE = 60_000L
            const val MILLIS_PER_WEEK = 7L * 24 * 60 * 60 * 1000
        }
    }
