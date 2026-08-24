package com.sangusantri.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.QuranReadingSessionEntity
import com.sangusantri.app.data.local.entity.QuranSurahEntity
import com.sangusantri.app.data.local.entity.QuranVerseEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Amalan Harian's "3 halaman" target is counted by joining a reading session's ayat range against
 * the mushaf page each ayat is printed on, so the join itself is the thing that has to be right —
 * a use-case test with hand-fed pages cannot prove it. Real Room, real query.
 *
 * The Hilt rule is not for injection — this test builds its own in-memory database. It exists
 * because the app's `@AndroidEntryPoint` boot receiver fires on every test-run reinstall, and
 * without a created component it takes the instrumentation process down before any test starts.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QuranReadingSessionDaoTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var database: SanguSantriDatabase

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    /** Ayat 1..5 sit on page 1, 6..10 on page 2 — a session covering 4..7 therefore spans two. */
    @Test
    fun aSessionReportsEveryMushafPageItsAyatRangeCovers() =
        runTest {
            seedSurah()
            insertSession(startAyat = 4, endAyat = 7)

            val pages = database.quranReadingSessionDao().observeSessionPages().first()

            assertEquals(listOf(1, 2), pages.map { it.page }.sorted())
        }

    /** Ten ayat on one page is one halaman, not ten — the `DISTINCT` the daily target relies on. */
    @Test
    fun manyAyatOnOnePageCountOnce() =
        runTest {
            seedSurah()
            insertSession(startAyat = 1, endAyat = 5)

            val pages = database.quranReadingSessionDao().observeSessionPages().first()

            assertEquals(listOf(1), pages.map { it.page })
        }

    @Test
    fun eachSessionKeepsItsOwnTimestampSoDaysCanBeSeparated() =
        runTest {
            seedSurah()
            insertSession(startAyat = 1, endAyat = 2, readAt = YESTERDAY_MILLIS)
            insertSession(startAyat = 6, endAyat = 7, readAt = TODAY_MILLIS)

            val pages = database.quranReadingSessionDao().observeSessionPages().first()

            assertEquals(
                setOf(YESTERDAY_MILLIS to 1, TODAY_MILLIS to 2),
                pages.mapTo(mutableSetOf()) { it.readAtEpochMillis to it.page },
            )
        }

    @Test
    fun aSessionWhoseVersesAreNotDownloadedContributesNoPages() =
        runTest {
            seedSurah()
            // Surah 2 has no verse rows in this database.
            insertSession(surahNumber = 2, startAyat = 1, endAyat = 10)

            val pages = database.quranReadingSessionDao().observeSessionPages().first()

            assertEquals(emptyList<Int>(), pages.map { it.page })
        }

    private suspend fun seedSurah() {
        database.quranSurahDao().insertAll(
            listOf(
                QuranSurahEntity(
                    number = 1,
                    latinName = "[FIXTURE] Surah",
                    arabicName = "[FIXTURE]",
                    meaning = "[FIXTURE]",
                    categoryArabic = "[FIXTURE]",
                    category = "[FIXTURE]",
                    ayatCount = VERSE_COUNT,
                ),
                QuranSurahEntity(
                    number = 2,
                    latinName = "[FIXTURE] Surah 2",
                    arabicName = "[FIXTURE]",
                    meaning = "[FIXTURE]",
                    categoryArabic = "[FIXTURE]",
                    category = "[FIXTURE]",
                    ayatCount = VERSE_COUNT,
                ),
            ),
        )
        database.quranVerseDao().insertAll(
            (1..VERSE_COUNT).map { ayat ->
                QuranVerseEntity(
                    surahNumber = 1,
                    ayatNumber = ayat,
                    remoteId = ayat.toLong(),
                    juz = 1,
                    page = if (ayat <= AYAT_PER_PAGE) 1 else 2,
                    arabicText = "[FIXTURE]",
                    arabicTextNoHarakat = "[FIXTURE]",
                    translation = "[FIXTURE]",
                    note = "",
                    footnoteNumber = "",
                    footnoteText = "",
                )
            },
        )
    }

    private suspend fun insertSession(
        surahNumber: Int = 1,
        startAyat: Int,
        endAyat: Int,
        readAt: Long = TODAY_MILLIS,
    ) {
        database.quranReadingSessionDao().insert(
            QuranReadingSessionEntity(
                surahNumber = surahNumber,
                startAyat = startAyat,
                endAyat = endAyat,
                readAtEpochMillis = readAt,
            ),
        )
    }

    private companion object {
        const val VERSE_COUNT = 10
        const val AYAT_PER_PAGE = 5
        const val TODAY_MILLIS = 1_800_000_000_000L
        const val YESTERDAY_MILLIS = TODAY_MILLIS - 86_400_000L
    }
}
