package com.sangusantri.app.domain.usecase

import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.model.QuranReadingPage
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsir
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.domain.repository.AmalanRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.domain.repository.TasbihRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * The rules from `docs/design/STREAK_GAMIFICATION_CONCEPT.md`, which are the whole feature: both
 * targets must land, udzur suspends the Qur'an half, an empty udzur day is bridged rather than
 * broken, and today never breaks a streak while it is still in progress.
 */
class ObserveAmalanHarianUseCaseTest {
    @Test
    fun dzikirAloneDoesNotCompleteTheDay() = runTest {
        val amalan = observe(dzikirOn = listOf(TODAY))

        assertTrue(amalan.dzikirDone)
        assertFalse(amalan.quranDone)
        assertFalse(amalan.isTodayComplete)
        assertEquals(0, amalan.currentStreakDays)
    }

    @Test
    fun twoPagesIsNotEnoughForTheQuranTarget() = runTest {
        val amalan = observe(dzikirOn = listOf(TODAY), pages = mapOf(TODAY to listOf(1, 2)))

        assertEquals(2, amalan.quranPagesToday)
        assertEquals(1, amalan.quranPagesLeft)
        assertFalse(amalan.isTodayComplete)
    }

    @Test
    fun bothTargetsCompleteTheDayAndStartTheStreak() = runTest {
        val amalan = observe(dzikirOn = listOf(TODAY), pages = mapOf(TODAY to listOf(1, 2, 3)))

        assertTrue(amalan.isTodayComplete)
        assertEquals(2, amalan.doneCount)
        assertEquals(1, amalan.currentStreakDays)
        assertEquals(AmalanDayState.COMPLETE, amalan.week.last().state)
    }

    /** The same page read three times is one page, which is what `DISTINCT` in the DAO guarantees. */
    @Test
    fun rereadingOnePageDoesNotCountThreeTimes() = runTest {
        val amalan = observe(dzikirOn = listOf(TODAY), pages = mapOf(TODAY to listOf(4, 4, 4)))

        assertEquals(1, amalan.quranPagesToday)
        assertFalse(amalan.isTodayComplete)
    }

    @Test
    fun consecutiveCompleteDaysAccumulate() = runTest {
        val days = listOf(TODAY, TODAY.minusDays(1), TODAY.minusDays(2))
        val amalan = observe(dzikirOn = days, pages = days.associateWith { listOf(1, 2, 3) })

        assertEquals(3, amalan.currentStreakDays)
        assertEquals(3, amalan.longestStreakDays)
    }

    @Test
    fun anIncompletePastDayBreaksTheStreakButKeepsTheRecord() = runTest {
        // Two complete days, then a gap, then two more complete days ending yesterday.
        val recent = listOf(TODAY.minusDays(1), TODAY.minusDays(2))
        val older = listOf(TODAY.minusDays(4), TODAY.minusDays(5), TODAY.minusDays(6))
        val complete = recent + older
        val amalan = observe(dzikirOn = complete, pages = complete.associateWith { listOf(1, 2, 3) })

        assertEquals(2, amalan.currentStreakDays)
        assertEquals(3, amalan.longestStreakDays)
    }

    /** Today is still in progress, so an empty today must not zero a streak earned yesterday. */
    @Test
    fun anEmptyTodayDoesNotBreakYesterdaysStreak() = runTest {
        val yesterday = listOf(TODAY.minusDays(1))
        val amalan = observe(dzikirOn = yesterday, pages = yesterday.associateWith { listOf(1, 2, 3) })

        assertEquals(1, amalan.currentStreakDays)
        assertEquals(AmalanDayState.PENDING, amalan.week.last().state)
    }

    @Test
    fun udzurSuspendsTheQuranTargetSoDzikirAloneCompletesTheDay() = runTest {
        val amalan = observe(dzikirOn = listOf(TODAY), udzur = setOf(TODAY), udzurActive = true)

        assertTrue(amalan.isUdzurToday)
        assertTrue(amalan.isTodayComplete)
        assertEquals(1, amalan.targetCount)
        assertEquals(1, amalan.currentStreakDays)
    }

    /** An udzur day with nothing recorded is bridged: the streak survives it without growing. */
    @Test
    fun anEmptyUdzurDayBridgesRatherThanBreaks() = runTest {
        val before = listOf(TODAY.minusDays(2), TODAY.minusDays(3))
        val amalan =
            observe(
                dzikirOn = before + TODAY,
                pages = (before + TODAY).associateWith { listOf(1, 2, 3) },
                udzur = setOf(TODAY.minusDays(1)),
            )

        // Today + the two days before the bridged one; the udzur day itself adds nothing.
        assertEquals(3, amalan.currentStreakDays)
        assertEquals(AmalanDayState.UDZUR, amalan.week[BRIDGED_DAY_INDEX].state)
    }

    @Test
    fun aMilestoneIsOfferedOnceAndTheLowerOnesAreAbsorbedWithIt() = runTest {
        val sevenDays = (0L until 7L).map { TODAY.minusDays(it) }
        val amalan = observe(dzikirOn = sevenDays, pages = sevenDays.associateWith { listOf(1, 2, 3) })

        assertEquals(7, amalan.currentStreakDays)
        // Nothing celebrated yet: the sheet offers 7, not 3 first.
        assertEquals(7, amalan.pendingMilestone(emptySet()))
        // Dismissing marks everything up to it, so nothing is offered again.
        assertEquals(null, amalan.pendingMilestone(setOf(3, 7)))
        // A streak short of the next milestone offers nothing.
        assertEquals(null, amalan.pendingMilestone(setOf(3, 7, 30)))
    }

    @Test
    fun theWeekStripIsAlwaysSevenDaysEndingToday() = runTest {
        val amalan = observe()

        assertEquals(7, amalan.week.size)
        assertEquals(TODAY, amalan.week.last().date)
        assertEquals(TODAY.minusDays(6), amalan.week.first().date)
    }

    private suspend fun observe(
        dzikirOn: List<LocalDate> = emptyList(),
        pages: Map<LocalDate, List<Int>> = emptyMap(),
        udzur: Set<LocalDate> = emptySet(),
        udzurActive: Boolean = false,
    ): AmalanHarian {
        val readingPages = pages.flatMap { (date, list) -> list.map { page -> readingPage(date, page) } }
        return ObserveAmalanHarianUseCase(
            tasbihRepository = FakeTasbihRepository(dzikirOn.map(::historyEntry)),
            quranRepository = FakeQuranPagesRepository(readingPages),
            amalanRepository = FakeAmalanRepository(udzur, udzurActive),
        ).invoke(ZONE).first()
    }

    private fun historyEntry(date: LocalDate): TasbihHistoryEntry = TasbihHistoryEntry(
        id = date.toEpochDay(),
        sessionName = null,
        targetValue = 33,
        finalCount = 33,
        startedAtEpochMillis = date.atMillis(),
        endedAtEpochMillis = date.atMillis(),
    )

    private fun readingPage(
        date: LocalDate,
        page: Int,
    ): QuranReadingPage = QuranReadingPage(readAtEpochMillis = date.atMillis(), page = page)

    private fun LocalDate.atMillis(): Long = ZonedDateTime.of(this, NOON, ZONE).toInstant().toEpochMilli()

    private companion object {
        val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
        val NOON: java.time.LocalTime = java.time.LocalTime.NOON
        val TODAY: LocalDate = LocalDate.now(ZONE)

        /** Index of `TODAY.minusDays(1)` in the seven-day strip (oldest first, today last). */
        const val BRIDGED_DAY_INDEX = 5
    }
}

private class FakeTasbihRepository(private val history: List<TasbihHistoryEntry>) : TasbihRepository {
    override fun observeSession(): Flow<TasbihSession?> = flowOf(null)

    override suspend fun incrementCount() = Unit

    override suspend fun startSession(
        targetPreset: TasbihTargetPreset,
        targetValue: Int?,
    ) = Unit

    override suspend fun renameSession(sessionName: String?) = Unit

    override suspend fun resetSession() = Unit

    override fun observeHistory(): Flow<List<TasbihHistoryEntry>> = flowOf(history)
}

private class FakeAmalanRepository(private val udzurDates: Set<LocalDate>, private val udzurActive: Boolean) :
    AmalanRepository {
    override fun observeUdzurDates(): Flow<Set<LocalDate>> = flowOf(udzurDates)

    override fun observeUdzurActive(): Flow<Boolean> = flowOf(udzurActive)

    override suspend fun setUdzurActive(active: Boolean) = Unit

    override fun observeCelebratedMilestones(): Flow<Set<Int>> = flowOf(emptySet())

    override suspend fun markMilestoneCelebrated(streakDays: Int) = Unit
}

private class FakeQuranPagesRepository(private val pages: List<QuranReadingPage>) : QuranRepository {
    override fun observeReadingPages(): Flow<List<QuranReadingPage>> = flowOf(pages)

    override fun observeSurahs(): Flow<List<QuranSurah>> = flowOf(emptyList())

    override fun observeVersesBySurah(surahNumber: Int): Flow<List<QuranVerse>> = flowOf(emptyList())

    override fun observeVersesByPageRange(
        fromPage: Int,
        toPage: Int,
    ): Flow<List<QuranVerse>> = flowOf(emptyList())

    override suspend fun pageOf(
        surahNumber: Int,
        ayatNumber: Int,
    ): Int? = null

    override fun observeJuzStarts(): Flow<List<QuranVerse>> = flowOf(emptyList())

    override fun observeBookmarks(): Flow<List<QuranBookmark>> = flowOf(emptyList())

    override fun observeIsBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Flow<Boolean> = flowOf(false)

    override fun observeReadingState(): Flow<QuranReadingState?> = flowOf(null)

    override fun observeReadingSessions(): Flow<List<QuranReadingSession>> = flowOf(emptyList())

    override suspend fun hasLocalDataset(): Boolean = true

    override suspend fun ensureInitialPreparation(
        onProgress: (completed: Int, total: Int) -> Unit,
    ): QuranPreparationResult = error("not used")

    override suspend fun toggleBookmark(
        surahNumber: Int,
        ayatNumber: Int,
    ) = Unit

    override suspend fun setLastRead(
        surahNumber: Int,
        ayatNumber: Int,
        page: Int,
    ) = Unit

    override suspend fun recordReadingSession(
        surahNumber: Int,
        startAyat: Int,
        endAyat: Int,
    ) = Unit

    override suspend fun getCachedTafsir(remoteAyatId: Long): QuranTafsir? = null

    override suspend fun fetchTafsir(remoteAyatId: Long): QuranTafsirResult = error("not used")
}
