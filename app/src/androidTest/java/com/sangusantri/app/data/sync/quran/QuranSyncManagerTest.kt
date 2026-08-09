package com.sangusantri.app.data.sync.quran

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.remote.quran.api.QuranApiService
import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranEnvelopeDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val EXPECTED_SURAH_COUNT = 114

/**
 * Exercises [QuranSyncManager] against a real HTTP stack (MockWebServer, a request-routing
 * [Dispatcher] since the manager fetches all 114 surahs' ayat concurrently) and a real in-memory
 * Room database — mirrors [com.sangusantri.app.data.sync.ContentSyncManagerTest]'s pattern.
 *
 * Every surah/ayat below is a `[FIXTURE]`-labelled synthetic value, never real Quran text — see
 * `CLAUDE.md` Content safety.
 */
@RunWith(AndroidJUnit4::class)
class QuranSyncManagerTest {
    private lateinit var server: MockWebServer
    private lateinit var database: SanguSantriDatabase
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SanguSantriDatabase::class.java)
                .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun completeSyncCommitsAllSurahsAndSortsOutOfOrderAyat() =
        runTest {
            // Surah 114's ayat arrive out of numeric order (2, 1, 3, 4, 5, 6) — the actually
            // observed LPMQ Kemenag response shape (docs/engineering/QURAN_API_CONTRACT_DRAFT.md).
            server.dispatcher = fixtureDispatcher(ayatOrderForSurah114 = listOf(2, 1, 3, 4, 5, 6))
            val manager = manager()

            val result = manager.sync(stableVersion = 2)

            assertTrue(result is QuranSyncResult.Completed)
            assertEquals(EXPECTED_SURAH_COUNT, database.quranSurahDao().count())
            val expectedVerseCount = (EXPECTED_SURAH_COUNT - 1) * AYAT_PER_SURAH + AYAT_PER_SURAH_114
            assertEquals(expectedVerseCount, database.quranVerseDao().count())
            val surah114Ayats = database.quranVerseDao().observeBySurah(114).first()
            assertEquals(listOf(1, 2, 3, 4, 5, 6), surah114Ayats.map { it.ayatNumber })
            val juzStarts = database.quranVerseDao().observeJuzStarts().first()
            assertEquals((1..30).toList(), juzStarts.map { it.juz })
            assertEquals(
                "2",
                database
                    .appMetadataDao()
                    .getByKey(QuranSyncMetadata.KEY_APPLIED_STABLE_VERSION)
                    ?.value,
            )
        }

    @Test
    fun atomicFailureLeavesNoPartialDataset() =
        runTest {
            server.dispatcher = fixtureDispatcher(failAyatForSurah = 50)
            val manager = manager()

            val result = manager.sync(stableVersion = 1)

            assertTrue(result is QuranSyncResult.RetryableFailure)
            assertEquals(0, database.quranSurahDao().count())
            assertEquals(0, database.quranVerseDao().count())
        }

    @Test
    fun refreshFailurePreservesPreviousCompleteDataset() =
        runTest {
            server.dispatcher = fixtureDispatcher()
            val manager = manager()
            assertTrue(manager.sync(stableVersion = 1) is QuranSyncResult.Completed)
            val surahCountBefore = database.quranSurahDao().count()
            val verseCountBefore = database.quranVerseDao().count()

            server.dispatcher = fixtureDispatcher(failAyatForSurah = 20)
            val refreshResult = manager.sync(stableVersion = 2)

            assertTrue(refreshResult is QuranSyncResult.RetryableFailure)
            assertEquals(surahCountBefore, database.quranSurahDao().count())
            assertEquals(verseCountBefore, database.quranVerseDao().count())
            assertEquals(
                "1",
                database
                    .appMetadataDao()
                    .getByKey(QuranSyncMetadata.KEY_APPLIED_STABLE_VERSION)
                    ?.value,
            )
        }

    @Test
    fun duplicateRemoteAyatIdAcrossSurahsIsRejectedAndNothingCommitted() =
        runTest {
            server.dispatcher = fixtureDispatcher(duplicateRemoteIdAcrossSurahs = true)
            val manager = manager()

            val result = manager.sync(stableVersion = 1)

            assertTrue(result is QuranSyncResult.PermanentFailure)
            assertEquals(0, database.quranSurahDao().count())
        }

    private fun manager(): QuranSyncManager {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        return QuranSyncManager(retrofit.create(QuranApiService::class.java), database)
    }

    /**
     * Routes by path rather than relying on enqueue order, since [QuranSyncManager] fetches all
     * 114 surahs' ayat concurrently (bounded concurrency) — arrival order at the server is not
     * deterministic.
     */
    @Suppress("LongParameterList")
    private fun fixtureDispatcher(
        ayatOrderForSurah114: List<Int> = listOf(1, 2, 3, 4, 5, 6),
        failAyatForSurah: Int? = null,
        duplicateRemoteIdAcrossSurahs: Boolean = false,
    ): Dispatcher =
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                return when {
                    path.startsWith("/surah/local/") -> jsonResponse(surahListEnvelope())
                    path.startsWith("/ayat/local/") -> {
                        val surahNumber = path.substringAfterLast("/").toInt()
                        if (surahNumber == failAyatForSurah) {
                            MockResponse().setResponseCode(500)
                        } else {
                            jsonResponse(
                                ayatEnvelope(
                                    surahNumber,
                                    order = if (surahNumber == 114) ayatOrderForSurah114 else listOf(1),
                                    // Surah 2 reuses surah 1's natural remote id (1 * 1000 + 1 =
                                    // 1001, see remoteIdOverride default below) to test cross-surah
                                    // uniqueness rejection, only when explicitly requested.
                                    remoteIdOverride =
                                        if (duplicateRemoteIdAcrossSurahs && surahNumber == 2) 1001L else null,
                                ),
                            )
                        }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

    private fun surahListEnvelope(): QuranEnvelopeDto<List<QuranSurahDto>> =
        QuranEnvelopeDto(
            code = 200,
            res = "success",
            data =
                (1..EXPECTED_SURAH_COUNT).map { number ->
                    QuranSurahDto(
                        id = number,
                        nama = "[FIXTURE] Surah $number",
                        arabic = "[FIXTURE-AR]",
                        arti = "[FIXTURE]",
                        kategoriAr = "[FIXTURE-AR]",
                        kategori = "Madaniyyah",
                        jumlahAyat = if (number == 114) AYAT_PER_SURAH_114 else AYAT_PER_SURAH,
                    )
                },
        )

    private fun ayatEnvelope(
        surahNumber: Int,
        order: List<Int>,
        remoteIdOverride: Long?,
    ): QuranEnvelopeDto<List<QuranAyatDto>> =
        QuranEnvelopeDto(
            code = 200,
            res = "success",
            data =
                order.map { ayatNumber ->
                    QuranAyatDto(
                        id = remoteIdOverride ?: (surahNumber * 1000L + ayatNumber),
                        surah = surahNumber,
                        ayat = ayatNumber,
                        juz = ((surahNumber - 1) % 30) + 1,
                        halaman = 1,
                        teksMsiUsmani = "[FIXTURE-AR]",
                        teksGundul = "[FIXTURE-AR]",
                        teksLatin = "[FIXTURE-LATIN]",
                        keterangan = "",
                        terjemah = "[FIXTURE]",
                        noFoot = "",
                        teksFoot = "",
                    )
                },
        )

    private inline fun <reified T> jsonResponse(body: QuranEnvelopeDto<T>): MockResponse =
        MockResponse().setBody(json.encodeToString(body))

    private companion object {
        const val AYAT_PER_SURAH = 1
        const val AYAT_PER_SURAH_114 = 6
    }
}
