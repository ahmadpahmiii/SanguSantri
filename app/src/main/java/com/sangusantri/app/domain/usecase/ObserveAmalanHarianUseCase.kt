package com.sangusantri.app.domain.usecase

import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.domain.model.QuranReadingPage
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.repository.AmalanRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Amalan Harian's read model: today's two targets, the streak they feed, and the seven-day strip
 * (`docs/design/STREAK_GAMIFICATION_CONCEPT.md`).
 *
 * Deliberately separate from [ObserveActivityOverviewUseCase], which reports history. This one
 * decides whether a day *counts*, which is a different question with its own rules: both targets
 * must land, udzur suspends the Qur'an half, and a day marked udzur with nothing recorded is
 * bridged rather than broken. Combining three repositories with real aggregation logic is exactly
 * where `CODING_STANDARD.md` says a use case belongs.
 */
class ObserveAmalanHarianUseCase
@Inject
constructor(
    private val tasbihRepository: TasbihRepository,
    private val quranRepository: QuranRepository,
    private val amalanRepository: AmalanRepository,
) {
    operator fun invoke(zoneId: ZoneId = ZoneId.systemDefault()): Flow<AmalanHarian> =
        combine(
            tasbihRepository.observeHistory(),
            quranRepository.observeReadingPages(),
            amalanRepository.observeUdzurDates(),
            amalanRepository.observeUdzurActive(),
        ) { tasbihHistory, readingPages, udzurDates, udzurActive ->
            build(tasbihHistory, readingPages, udzurDates, udzurActive, zoneId)
        }

    private fun build(
        tasbihHistory: List<TasbihHistoryEntry>,
        readingPages: List<QuranReadingPage>,
        udzurDates: Set<LocalDate>,
        udzurActive: Boolean,
        zoneId: ZoneId,
    ): AmalanHarian {
        val today = LocalDate.now(zoneId)
        val dzikirDays = tasbihHistory.mapTo(mutableSetOf()) { it.endedAtEpochMillis.toLocalDate(zoneId) }
        val pagesPerDay =
            readingPages
                .groupBy { it.readAtEpochMillis.toLocalDate(zoneId) }
                .mapValues { (_, pages) -> pages.mapTo(mutableSetOf()) { it.page }.size }
        // An open udzur period covers today even before the toggle's date lands in storage.
        val udzur = if (udzurActive) udzurDates + today else udzurDates

        val outcome = { date: LocalDate -> outcomeOf(date, dzikirDays, pagesPerDay, udzur) }
        val earliest = (dzikirDays + pagesPerDay.keys + udzur).minOrNull()
        val longest = longestStreak(earliest, today, outcome)

        return AmalanHarian(
            dzikirDone = today in dzikirDays,
            quranPagesToday = pagesPerDay[today] ?: 0,
            isUdzurToday = today in udzur,
            currentStreakDays = currentStreak(today, outcome),
            longestStreakDays = longest,
            week = week(today, outcome),
            hasEverCompleted = longest > 0,
        )
    }

    private fun outcomeOf(
        date: LocalDate,
        dzikirDays: Set<LocalDate>,
        pagesPerDay: Map<LocalDate, Int>,
        udzurDates: Set<LocalDate>,
    ): AmalanDayState {
        val dzikir = date in dzikirDays
        if (date in udzurDates) return if (dzikir) AmalanDayState.COMPLETE else AmalanDayState.UDZUR
        val pages = pagesPerDay[date] ?: 0
        return if (dzikir && pages >= AmalanHarian.QURAN_PAGE_TARGET) {
            AmalanDayState.COMPLETE
        } else {
            AmalanDayState.INCOMPLETE
        }
    }

    /**
     * Counts back from today. Today is never allowed to break the streak — it is still in progress
     * — and an udzur day with nothing recorded is stepped over without adding to the count.
     */
    private fun currentStreak(
        today: LocalDate,
        outcome: (LocalDate) -> AmalanDayState,
    ): Int {
        var streak = 0
        var cursor = today
        // ponytail: bounded walk rather than "until the data runs out" — a phone left in udzur for
        // a year would otherwise be an unbounded loop. Raise it if a streak ever gets near it.
        repeat(MAX_LOOKBACK_DAYS) {
            when (outcome(cursor)) {
                AmalanDayState.COMPLETE -> streak++
                AmalanDayState.UDZUR -> Unit
                AmalanDayState.INCOMPLETE -> if (cursor != today) return streak
                AmalanDayState.PENDING -> Unit
            }
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private fun longestStreak(
        earliest: LocalDate?,
        today: LocalDate,
        outcome: (LocalDate) -> AmalanDayState,
    ): Int {
        val start = earliest ?: return 0
        var longest = 0
        var run = 0
        var cursor = maxOf(start, today.minusDays(MAX_LOOKBACK_DAYS.toLong()))
        while (!cursor.isAfter(today)) {
            when (outcome(cursor)) {
                AmalanDayState.COMPLETE -> {
                    run++
                    longest = maxOf(longest, run)
                }

                AmalanDayState.UDZUR, AmalanDayState.PENDING -> Unit
                AmalanDayState.INCOMPLETE -> run = 0
            }
            cursor = cursor.plusDays(1)
        }
        return longest
    }

    /** The seven-day strip, oldest first, ending today. */
    private fun week(
        today: LocalDate,
        outcome: (LocalDate) -> AmalanDayState,
    ): List<AmalanDay> =
        (WEEK_LENGTH - 1 downTo 0).map { back ->
            val date = today.minusDays(back.toLong())
            val state = outcome(date)
            AmalanDay(
                date = date,
                // Today has not failed at anything yet, so it shows as pending rather than missed.
                state = if (date == today && state == AmalanDayState.INCOMPLETE) AmalanDayState.PENDING else state,
            )
        }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    private companion object {
        const val WEEK_LENGTH = 7
        const val MAX_LOOKBACK_DAYS = 400
    }
}
