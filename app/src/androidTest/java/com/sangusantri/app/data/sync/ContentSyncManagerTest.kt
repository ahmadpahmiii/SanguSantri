package com.sangusantri.app.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
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
 * Exercises [ContentSyncManager] against a real HTTP stack (MockWebServer) and a real Room
 * database, on the CMS API's `schemaVersion` 2 contract.
 *
 * This covers the *list* tier only — the two metadata listings Beranda refreshes on every resume.
 * Steps arrive through [ContentDetailSyncManager] when a reader opens an item; that split is what
 * lets this run so often, and is covered by ContentDetailSyncManagerTest.
 *
 * What these pin is the behaviour that replaced per-item versioning. The server no longer says
 * which items changed, so the sync must work that out against Room — and must be conservative
 * about it in both directions: never rewrite an item nobody edited (that would throw away reading
 * positions and guided-session state), and never hide an item just because a request failed.
 *
 * `@HiltAndroidTest` even though nothing here is injected: this app's `ReminderBootReceiver` is an
 * `@AndroidEntryPoint` broadcast receiver, and a broadcast reaching the instrumented process
 * without a Hilt component crashes the run.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ContentSyncManagerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var server: MockWebServer
    private lateinit var database: SanguSantriDatabase
    private lateinit var manager: ContentSyncManager
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
        manager = ContentSyncManager(retrofit.create(ContentApiService::class.java), ContentImporter(database))
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    /** A full sync is two requests — one per category — and never a per-item follow-up. */
    @Test
    fun syncIssuesExactlyOneRequestPerCategory() =
        runTest {
            server.enqueue(categoryResponse(item("salamun-salam")))
            server.enqueue(categoryResponse(item("tahlil")))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(2, server.requestCount)
            assertEquals(setOf("/api/v1/sholawat", "/api/v1/amaliyah"), requestedPaths())
        }

    /**
     * A new item gets a visible row from the list alone, with no steps yet. That is deliberate: the
     * reader has to be able to see an item before tapping it is what fetches its detail.
     */
    @Test
    fun newItemBecomesVisibleFromTheListWithoutSteps() =
        runTest {
            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("tahlil")))

            val result = manager.sync()

            assertEquals(listOf("tahlil"), (result as SyncResult.Completed).updatedVersionIds)
            val row = database.contentDao().getById("tahlil")
            assertEquals(true, row?.isActive)
            assertEquals("Sample", row?.title)
            assertEquals(0, database.contentStepDao().countByContentId("tahlil"))
        }

    /**
     * A list sync must never disturb steps or progress — it does not even carry steps. This is what
     * makes running it on every Beranda resume safe.
     */
    @Test
    fun listSyncNeverTouchesStepsOrReadingPosition() =
        runTest {
            seed("tahlil")
            database.readingPositionDao().upsert(ReadingPositionEntity("tahlil", 7, 42, 1_000L))

            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("tahlil")))

            val result = manager.sync()

            val completed = result as SyncResult.Completed
            assertEquals(listOf("tahlil"), completed.skippedVersionIds)
            assertTrue(completed.updatedVersionIds.isEmpty())
            assertEquals(1, database.contentDao().getById("tahlil")?.version)
            assertEquals(7, database.readingPositionDao().getByContentId("tahlil")?.itemIndex)
        }

    /** Renaming or reordering must not count as a content change. */
    @Test
    fun metadataOnlyEditUpdatesTheRowWithoutTouchingProgress() =
        runTest {
            seed("tahlil")
            database.readingPositionDao().upsert(ReadingPositionEntity("tahlil", 7, 42, 1_000L))

            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("tahlil").copy(title = "Tahlil Lengkap", order = 9)))

            val result = manager.sync()

            assertEquals(listOf("tahlil"), (result as SyncResult.Completed).skippedVersionIds)
            val row = database.contentDao().getById("tahlil")
            assertEquals("Tahlil Lengkap", row?.title)
            assertEquals(9, row?.order)
            assertEquals(1, row?.version)
            assertEquals(7, database.readingPositionDao().getByContentId("tahlil")?.itemIndex)
        }

    /**
     * Unpublishing is "stop distributing", not "recall": the item leaves Beranda but its row and
     * steps stay, so a reader part-way through it does not lose the text.
     */
    @Test
    fun itemMissingFromTheResponseIsHiddenButNotDeleted() =
        runTest {
            seed("tahlil")
            seed("istighosah")

            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("istighosah")))

            manager.sync()

            val row = database.contentDao().getById("tahlil")
            assertNotNull(row)
            assertEquals(false, row?.isActive)
            assertTrue(database.contentStepDao().countByContentId("tahlil") > 0)
            assertEquals(true, database.contentDao().getById("istighosah")?.isActive)
        }

    /**
     * A response with no items at all hides nothing, deliberately. Unpublishing the entire
     * catalogue is not a thing anyone does on purpose, and the cost of believing it — a device
     * whose Beranda is empty until someone notices — is far worse than the cost of ignoring it.
     * Individual unpublishes still take effect (the test above); only the all-gone case is refused.
     */
    @Test
    fun anEntirelyEmptyResponseHidesNothing() =
        runTest {
            seed("tahlil")

            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse())

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(true, database.contentDao().getById("tahlil")?.isActive)
        }

    /** Re-publishing brings it back without re-importing the steps. */
    @Test
    fun republishedItemBecomesActiveAgain() =
        runTest {
            seed("tahlil", active = false)

            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("tahlil")))

            manager.sync()

            assertEquals(true, database.contentDao().getById("tahlil")?.isActive)
            assertEquals(1, database.contentDao().getById("tahlil")?.version)
        }

    /**
     * The dangerous case. A failed second request must not be read as "amaliyah is empty now" —
     * that would hide every amaliyah on the device because the network blipped.
     */
    @Test
    fun failureOnTheSecondCategoryHidesNothing() =
        runTest {
            seed("tahlil")

            server.enqueue(categoryResponse())
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertEquals(true, database.contentDao().getById("tahlil")?.isActive)
        }

    @Test
    fun http500ReturnsRetryableFailureAndWritesNothing() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertNull(database.contentDao().getById("tahlil"))
        }

    @Test
    fun nonRetryableHttpStatusReturnsPermanentFailure() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400))

            assertTrue(manager.sync() is SyncResult.PermanentFailure)
        }

    /** An unknown schemaVersion means the app keeps its cache rather than parsing half a contract. */
    @Test
    fun unsupportedSchemaVersionIsRejectedWithoutWriting() =
        runTest {
            seed("tahlil")
            server.enqueue(
                MockResponse().setBody(
                    json.encodeToString(ContentListResponseDto(schemaVersion = 99, items = listOf(item("x")))),
                ),
            )

            val result = manager.sync()

            assertTrue(result is SyncResult.PermanentFailure)
            assertNull(database.contentDao().getById("x"))
            assertEquals(true, database.contentDao().getById("tahlil")?.isActive)
        }

    /**
     * One malformed entry fails the whole listing. A half-imported catalogue leaves Beranda showing
     * an arbitrary subset with nothing to say which items are missing.
     */
    @Test
    fun oneInvalidItemRejectsTheWholeResponse() =
        runTest {
            server.enqueue(categoryResponse())
            server.enqueue(categoryResponse(item("ok"), item("bad").copy(title = "")))

            val result = manager.sync()

            assertTrue(result is SyncResult.PermanentFailure)
            assertNull(database.contentDao().getById("ok"))
        }

    /** Seeds a fully-imported item: list row plus its steps, as a device that has opened it holds. */
    private suspend fun seed(
        id: String,
        active: Boolean = true,
    ) {
        val importer = ContentImporter(database)
        importer.importListItem(item(id))
        importer.importRemoteDetail(detail(id))
        if (!active) {
            database.contentDao().deactivateAbsent(listOf("nothing-matches-this"))
        }
    }

    private fun requestedPaths(): Set<String> =
        buildSet {
            repeat(server.requestCount) { add(server.takeRequest().path.orEmpty()) }
        }

    private fun categoryResponse(vararg items: ContentListItemDto): MockResponse =
        MockResponse().setBody(
            json.encodeToString(ContentListResponseDto(schemaVersion = 3, items = items.toList())),
        )

    private fun item(id: String) =
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
