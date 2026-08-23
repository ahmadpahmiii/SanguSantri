package com.sangusantri.app.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.data.remote.api.ContentApiService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * The detail tier: what happens when a reader opens an item.
 *
 * These pin the offline-first contract at its sharpest point. The reader renders from Room and the
 * network only updates it, so a failed refresh must be invisible, and a successful one must not
 * rewrite content that did not actually change — rewriting steps drops the reader's saved position
 * and guided-session state, which is a real loss for an item nobody edited.
 *
 * `@HiltAndroidTest` even though nothing here is injected: this app's `ReminderBootReceiver` is an
 * `@AndroidEntryPoint` broadcast receiver, and a broadcast reaching the instrumented process
 * without a Hilt component crashes the run.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ContentDetailSyncManagerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var server: MockWebServer
    private lateinit var database: SanguSantriDatabase
    private lateinit var manager: ContentDetailSyncManager
    private lateinit var importer: ContentImporter
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), SanguSantriDatabase::class.java)
                .build()
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        importer = ContentImporter(database)
        manager = ContentDetailSyncManager(retrofit.create(ContentApiService::class.java), importer)
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    /** The first open of an item the list created: this fetch *is* the load. */
    @Test
    fun firstOpenFetchesStepsForAListOnlyRow() =
        runTest {
            importer.importListItem(listItem("tahlil"))
            assertEquals(0, database.contentStepDao().countByContentId("tahlil"))

            server.enqueue(detailResponse(detail("tahlil")))

            assertTrue(manager.refresh("tahlil", isSholawat = false))
            assertEquals(1, database.contentStepDao().countByContentId("tahlil"))
            // Source attribution arrives with the detail; the list row had none to give.
            assertEquals("NON-PRODUCTION FIXTURE", database.contentDao().getById("tahlil")?.sourceName)
        }

    @Test
    fun categoryPicksTheRoute() =
        runTest {
            importer.importListItem(listItem("salamun-salam"))
            server.enqueue(detailResponse(detail("salamun-salam")))

            manager.refresh("salamun-salam", isSholawat = true)

            assertEquals("/api/v1/sholawat/salamun-salam", server.takeRequest().path)
        }

    /**
     * Re-opening an unchanged item must leave everything alone. The ETag usually spares the body
     * entirely, but a 200 still has to be checked — this is the guard that keeps a reader's saved
     * position through a refresh that changed nothing.
     */
    @Test
    fun unchangedDetailKeepsStepsRevisionAndReadingPosition() =
        runTest {
            seed("tahlil")
            database.readingPositionDao().upsert(ReadingPositionEntity("tahlil", 7, 42, 1_000L))
            val versionBefore = database.contentDao().getById("tahlil")?.version

            server.enqueue(detailResponse(detail("tahlil")))

            assertFalse("unchanged content must not report a change", manager.refresh("tahlil", isSholawat = false))
            assertEquals(versionBefore, database.contentDao().getById("tahlil")?.version)
            assertEquals(7, database.readingPositionDao().getByContentId("tahlil")?.itemIndex)
        }

    /** An edit is a real change: steps replaced, local revision bumped, stale position dropped. */
    @Test
    fun editedDetailReplacesStepsAndBumpsLocalVersion() =
        runTest {
            seed("tahlil")
            database.readingPositionDao().upsert(ReadingPositionEntity("tahlil", 7, 42, 1_000L))
            val versionBefore = database.contentDao().getById("tahlil")?.version ?: 0

            server.enqueue(detailResponse(detail("tahlil", arabic = "[FIXTURE-AR] edited")))

            assertTrue(manager.refresh("tahlil", isSholawat = false))
            assertEquals(versionBefore + 1, database.contentDao().getById("tahlil")?.version)
            val edited = database.contentStepDao().getByContentId("tahlil")
            assertEquals("[FIXTURE-AR] edited", edited.first().arabicText)
            assertNull(database.readingPositionDao().getByContentId("tahlil"))
        }

    /** Offline is the normal case for this app, not an error: the cached copy stands. */
    @Test
    fun networkFailureLeavesTheCachedCopyIntact() =
        runTest {
            seed("tahlil")
            server.shutdown()

            assertFalse(manager.refresh("tahlil", isSholawat = false))
            assertEquals(1, database.contentStepDao().countByContentId("tahlil"))
            assertNotNull(database.contentDao().getById("tahlil"))
        }

    /** Unpublished between the list sync and this open. Nothing to do — the cached copy still reads. */
    @Test
    fun notFoundLeavesTheCachedCopyIntact() =
        runTest {
            seed("tahlil")
            server.enqueue(MockResponse().setResponseCode(404))

            assertFalse(manager.refresh("tahlil", isSholawat = false))
            assertEquals(1, database.contentStepDao().countByContentId("tahlil"))
        }

    /** A detail answering with the wrong id would otherwise overwrite the wrong item's steps. */
    @Test
    fun mismatchedIdIsRefused() =
        runTest {
            seed("tahlil")
            server.enqueue(detailResponse(detail("istighosah", arabic = "[FIXTURE-AR] other")))

            assertFalse(manager.refresh("tahlil", isSholawat = false))
            val steps = database.contentStepDao().getByContentId("tahlil")
            assertEquals("[FIXTURE-AR]", steps.first().arabicText)
        }

    /** An unreadable detail must not half-import; the reader keeps what it had. */
    @Test
    fun invalidDetailIsRejectedWithoutWriting() =
        runTest {
            seed("tahlil")
            server.enqueue(detailResponse(detail("tahlil", arabic = "edited").copy(sourceName = "")))

            assertFalse(manager.refresh("tahlil", isSholawat = false))
            val steps = database.contentStepDao().getByContentId("tahlil")
            assertEquals("[FIXTURE-AR]", steps.first().arabicText)
        }

    private suspend fun seed(id: String) {
        importer.importListItem(listItem(id))
        importer.importRemoteDetail(detail(id))
    }

    private fun detailResponse(detail: ContentDetailDto): MockResponse =
        MockResponse().setBody(json.encodeToString(detail))

    private fun listItem(id: String) =
        ContentListItemDto(
            id = id,
            title = "Sample",
            description = "[FIXTURE] Sample",
            imageUrl = null,
            category = "Amaliyah",
            order = 1,
        )

    private fun detail(
        id: String,
        arabic: String = "[FIXTURE-AR]",
    ) = ContentDetailDto(
        schemaVersion = 3,
        id = id,
        title = "Sample",
        description = "[FIXTURE] Sample",
        imageUrl = null,
        category = "Amaliyah",
        order = 1,
        sourceName = "NON-PRODUCTION FIXTURE",
        sourceUrl = "https://example.invalid/fixture",
        steps =
            listOf(
                ContentStepDto(
                    id = "$id-step-01",
                    arabicText = arabic,
                    translation = "[FIXTURE]",
                    repeatTarget = null,
                ),
            ),
    )
}
