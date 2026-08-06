package com.sangusantri.app.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.ContentValidator
import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.remote.api.ContentApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Exercises [ContentSyncManager] against a real HTTP stack (MockWebServer) and a real Room
 * database (ADR 0015): a matching remote version is skipped without a content-file download, a
 * newer remote version downloads and replaces content, a catalog or content-file timeout/HTTP 500
 * returns [SyncResult.RetryableFailure], a retry after a content-file failure re-attempts only the
 * item not yet imported, and a permanently invalid content file is rejected while another item in
 * the same catalog still imports.
 */
@RunWith(AndroidJUnit4::class)
class ContentSyncManagerTest {
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
        val api = retrofit.create(ContentApiService::class.java)
        manager = ContentSyncManager(api, ContentImporter(database))
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun matchingRemoteVersionSkipsContentFileDownload() =
        runTest {
            ContentImporter(database).importContentFile(item(version = 1), file(version = 1))

            server.enqueue(catalogResponse(item(version = 1)))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            val completed = result as SyncResult.Completed
            assertEquals(listOf("sample"), completed.skippedVersionIds)
            assertTrue(completed.updatedVersionIds.isEmpty())
            // Only the catalog request — no content-file endpoint call for an up-to-date item.
            assertEquals(1, server.requestCount)
        }

    @Test
    fun newerRemoteVersionDownloadsAndReplacesContent() =
        runTest {
            ContentImporter(database).importContentFile(item(version = 1), file(version = 1))

            server.enqueue(catalogResponse(item(version = 2)))
            server.enqueue(contentResponse(file(version = 2)))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(listOf("sample"), (result as SyncResult.Completed).updatedVersionIds)
            assertEquals(2, database.contentDao().getById("sample")?.version)
        }

    @Test
    fun catalogHttp500ReturnsRetryableFailure() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun catalogNonRetryableHttpStatusReturnsPermanentFailure() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400))

            val result = manager.sync()

            assertTrue(result is SyncResult.PermanentFailure)
        }

    @Test
    fun contentFileTimeoutOrHttp500ReturnsRetryableFailure() =
        runTest {
            server.enqueue(catalogResponse(item(version = 1)))
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun retryAfterContentFailureSkipsAlreadyImportedItems() =
        runTest {
            val ok = item(id = "ok", version = 1)
            val broken = item(id = "broken", version = 1)

            // First attempt: ok's content file succeeds, broken's content file times out (HTTP 500).
            server.enqueue(catalogResponse(ok, broken))
            server.enqueue(contentResponse(file(id = "ok", version = 1)))
            server.enqueue(MockResponse().setResponseCode(500))

            val firstAttempt = manager.sync()
            assertTrue(firstAttempt is SyncResult.RetryableFailure)
            assertEquals(1, database.contentDao().getById("ok")?.version)

            // Second attempt (the "retry"): same catalog, broken's content file now succeeds. ok is
            // already up to date in Room, so only broken's content endpoint should be requested again.
            server.enqueue(catalogResponse(ok, broken))
            server.enqueue(contentResponse(file(id = "broken", version = 1)))
            val requestsBeforeRetry = server.requestCount

            val secondAttempt = manager.sync()

            assertTrue(secondAttempt is SyncResult.Completed)
            val completed = secondAttempt as SyncResult.Completed
            assertEquals(listOf("broken"), completed.updatedVersionIds)
            assertEquals(listOf("ok"), completed.skippedVersionIds)
            // Catalog + broken's content file only — ok's content endpoint was never called again.
            assertEquals(requestsBeforeRetry + 2, server.requestCount)
        }

    @Test
    fun permanentlyInvalidContentIsRejectedWhileAnotherValidItemStillImports() =
        runTest {
            val ok = item(id = "ok", version = 1)
            val invalid = item(id = "bad", version = 1)
            // Structurally invalid (no steps) — a permanent rejection ContentValidator itself makes,
            // never worth retrying.
            val invalidFile = file(id = "bad", version = 1).copy(steps = emptyList())

            server.enqueue(catalogResponse(ok, invalid))
            server.enqueue(contentResponse(file(id = "ok", version = 1)))
            server.enqueue(contentResponse(invalidFile))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            val completed = result as SyncResult.Completed
            assertEquals(listOf("ok"), completed.updatedVersionIds)
            assertEquals(listOf("bad"), completed.rejectedVersionIds)
            assertNull(database.contentDao().getById("bad"))
        }

    private fun catalogResponse(vararg items: ContentCatalogItemDto): MockResponse =
        MockResponse().setBody(json.encodeToString(ContentCatalogDto(schemaVersion = 1, items = items.toList())))

    private fun contentResponse(file: ContentFileDto): MockResponse = MockResponse().setBody(json.encodeToString(file))

    private fun item(
        id: String = "sample",
        version: Int,
    ) = ContentCatalogItemDto(
        id = id,
        title = "Sample",
        description = "[FIXTURE] Sample",
        imageUrl = null,
        category = "Tahlil dan Doa",
        version = version,
        contentUrl = "/content/packages/$id-v$version.json",
        order = 1,
        isActive = true,
    )

    private fun file(
        id: String = "sample",
        version: Int,
    ) = ContentFileDto(
        schemaVersion = ContentValidator.SUPPORTED_SCHEMA_VERSION,
        id = id,
        version = version,
        sourceName = "NON-PRODUCTION FIXTURE",
        sourceUrl = "https://example.invalid/fixture",
        steps =
            listOf(
                ContentStepDto(
                    id = "$id-v$version-step-01",
                    arabicText = "[FIXTURE-AR]",
                    translation = "[FIXTURE]",
                    repeatTarget = 1,
                ),
            ),
    )
}
