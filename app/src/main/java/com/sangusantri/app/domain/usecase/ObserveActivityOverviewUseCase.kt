package com.sangusantri.app.domain.usecase

import com.sangusantri.app.domain.model.ActivityOverview
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import com.sangusantri.app.domain.model.QuranActivityEntry
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.repository.ActivityRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Combines [ActivityRepository] (amaliyah completions), [TasbihRepository] (tasbih history),
 * [ReminderRepository] (`0.0.4`, upcoming reminders), and [QuranRepository] (`0.0.6`, reading
 * sessions — reused directly rather than duplicated, `docs/engineering/ARCHITECTURE.md`'s
 * per-concern-repository convention) into the read model Aktivitas needs: streak, this-week
 * summary, and the recent-5 preview lists. Combining multiple repositories with genuine aggregation
 * logic (streak/weekly-window math) is exactly when `CODING_STANDARD.md` says a use case is
 * warranted.
 */
class ObserveActivityOverviewUseCase
    @Inject
    constructor(
        private val activityRepository: ActivityRepository,
        private val tasbihRepository: TasbihRepository,
        private val reminderRepository: ReminderRepository,
        private val quranRepository: QuranRepository,
    ) {
        operator fun invoke(zoneId: ZoneId = ZoneId.systemDefault()): Flow<ActivityOverview> =
            combine(
                activityRepository.observeCompletions(),
                tasbihRepository.observeHistory(),
                reminderRepository.observeAll(),
                quranRepository.observeReadingSessions(),
                quranRepository.observeSurahs(),
            ) { completions, tasbihHistory, reminders, quranSessions, surahs ->
                buildOverview(completions, tasbihHistory, reminders, quranSessions, surahs, zoneId)
            }

    @Suppress("LongParameterList")
        private fun buildOverview(
            completions: List<AmaliyahCompletionEvent>,
            tasbihHistory: List<TasbihHistoryEntry>,
            reminders: List<Reminder>,
            quranSessions: List<QuranReadingSession>,
            surahs: List<QuranSurah>,
            zoneId: ZoneId,
        ): ActivityOverview {
            val now = System.currentTimeMillis()
            val activeDates =
                (
                    completions.map { it.completedAtEpochMillis } +
                        tasbihHistory.map { it.endedAtEpochMillis } +
                        quranSessions.map { it.readAtEpochMillis }
                    ).mapTo(mutableSetOf()) { epochMillisToLocalDate(it, zoneId) }

            val weekStart = now - MILLIS_PER_WEEK
            val weeklyCompletions = completions.filter { it.completedAtEpochMillis >= weekStart }
            val weeklyTasbih = tasbihHistory.filter { it.endedAtEpochMillis >= weekStart }
        val weeklyQuranSessions = quranSessions.filter { it.readAtEpochMillis >= weekStart }
            val weeklyDurationMillis =
                weeklyCompletions.sumOf { it.durationMillis } +
                    weeklyTasbih.sumOf { it.endedAtEpochMillis - it.startedAtEpochMillis }

        val surahNames = surahs.associate { it.number to it.latinName }

            return ActivityOverview(
                currentStreakDays = calculateCurrentStreak(activeDates, epochMillisToLocalDate(now, zoneId)),
                longestStreakDays = calculateLongestStreak(activeDates),
                weeklyAmaliyahCompletedCount = weeklyCompletions.size,
                weeklyTasbihSessionCount = weeklyTasbih.size,
                weeklyTotalMinutes = weeklyDurationMillis / MILLIS_PER_MINUTE,
                recentAmaliyahCompletions = completions.take(RECENT_LIMIT),
                recentTasbihHistory = tasbihHistory.take(RECENT_LIMIT),
                // ReminderRepository.observeAll() is already ordered soonest-first.
                upcomingReminders = reminders.filter { it.isEnabled }.take(RECENT_LIMIT),
                weeklyQuranSessionCount = weeklyQuranSessions.size,
                recentQuranSessions =
                    quranSessions.take(RECENT_LIMIT).map { session ->
                        QuranActivityEntry(
                            surahNumber = session.surahNumber,
                            surahName = surahNames[session.surahNumber].orEmpty(),
                            startAyat = session.startAyat,
                            endAyat = session.endAyat,
                            readAtEpochMillis = session.readAtEpochMillis,
                        )
                    },
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
