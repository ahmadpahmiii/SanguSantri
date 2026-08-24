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
import javax.inject.Inject

/**
 * Combines [ActivityRepository] (amaliyah completions), [TasbihRepository] (tasbih history),
 * [ReminderRepository] (`0.0.4`, upcoming reminders), and [QuranRepository] (`0.0.6`, reading
 * sessions — reused directly rather than duplicated, `docs/engineering/ARCHITECTURE.md`'s
 * per-concern-repository convention) into the read model Aktivitas' history needs: the this-week
 * summary and the recent-5 preview lists. Combining multiple repositories with genuine aggregation
 * logic (the weekly window) is exactly when `CODING_STANDARD.md` says a use case is warranted.
 *
 * The streak is deliberately **not** here — `ObserveAmalanHarianUseCase` owns it, because "did this
 * day count" follows Amalan Harian's two-target rule rather than "did anything happen".
 */
class ObserveActivityOverviewUseCase
@Inject
constructor(
    private val activityRepository: ActivityRepository,
    private val tasbihRepository: TasbihRepository,
    private val reminderRepository: ReminderRepository,
    private val quranRepository: QuranRepository,
) {
    operator fun invoke(): Flow<ActivityOverview> =
        combine(
            activityRepository.observeCompletions(),
            tasbihRepository.observeHistory(),
            reminderRepository.observeAll(),
            quranRepository.observeReadingSessions(),
            quranRepository.observeSurahs(),
        ) { completions, tasbihHistory, reminders, quranSessions, surahs ->
            buildOverview(completions, tasbihHistory, reminders, quranSessions, surahs)
        }

    @Suppress("LongParameterList")
    private fun buildOverview(
        completions: List<AmaliyahCompletionEvent>,
        tasbihHistory: List<TasbihHistoryEntry>,
        reminders: List<Reminder>,
        quranSessions: List<QuranReadingSession>,
        surahs: List<QuranSurah>,
    ): ActivityOverview {
        val now = System.currentTimeMillis()
        val weekStart = now - MILLIS_PER_WEEK
        val weeklyCompletions = completions.filter { it.completedAtEpochMillis >= weekStart }
        val weeklyTasbih = tasbihHistory.filter { it.endedAtEpochMillis >= weekStart }
        val weeklyQuranSessions = quranSessions.filter { it.readAtEpochMillis >= weekStart }
        val weeklyDurationMillis =
            weeklyCompletions.sumOf { it.durationMillis } +
                weeklyTasbih.sumOf { it.endedAtEpochMillis - it.startedAtEpochMillis }

        val surahNames = surahs.associate { it.number to it.latinName }

        return ActivityOverview(
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

    private companion object {
        const val RECENT_LIMIT = 5
        const val MILLIS_PER_MINUTE = 60_000L
        const val MILLIS_PER_WEEK = 7L * 24 * 60 * 60 * 1000
    }
}
