package com.sangusantri.app.data.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.ContentChecksum
import com.sangusantri.app.data.content.ContentPackageImporter
import com.sangusantri.app.data.content.dto.AmaliyahDto
import com.sangusantri.app.data.content.dto.AmaliyahStepDto
import com.sangusantri.app.data.content.dto.AmaliyahVariantDto
import com.sangusantri.app.data.content.dto.AmaliyahVersionDto
import com.sangusantri.app.data.content.dto.ApprovalDto
import com.sangusantri.app.data.content.dto.ContentPackageDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.remote.api.ContentApiService
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
 * database, covering the high-risk remote-sync behaviours (section 19): a matching remote version
 * is skipped without a package download, a newer remote version downloads and replaces content, a
 * manifest or package timeout/HTTP 500 returns [SyncResult.RetryableFailure], a retry after a
 * package failure re-attempts only the package not yet imported, and a permanently invalid package
 * is rejected while another package in the same manifest still imports.
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
        val importer = ContentPackageImporter(database)
        manager = ContentSyncManager(api, importer, ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun matchingRemoteVersionSkipsPackageDownload() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            val checksum = ContentChecksum.sha256Hex(bytes)
            ContentPackageImporter(database).importPackage(bytes, pkg.version.id, checksum)

            server.enqueue(MockResponse().setBody(manifestBody(pkg, checksum)))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            val completed = result as SyncResult.Completed
            assertEquals(listOf(pkg.version.id), completed.skippedVersionIds)
            assertTrue(completed.updatedVersionIds.isEmpty())
            // Only the manifest request — no package endpoint call for an up-to-date package.
            assertEquals(1, server.requestCount)
        }

    @Test
    fun newerRemoteVersionDownloadsAndReplacesContent() =
        runTest {
            val oldPkg = packageFor(versionNumber = 1)
            val oldBytes = json.encodeToString(oldPkg).encodeToByteArray()
            ContentPackageImporter(database).importPackage(
                oldBytes,
                oldPkg.version.id,
                ContentChecksum.sha256Hex(oldBytes),
            )

            val newPkg = packageFor(versionNumber = 2)
            val newBytes = json.encodeToString(newPkg).encodeToByteArray()
            val newChecksum = ContentChecksum.sha256Hex(newBytes)

            server.enqueue(MockResponse().setBody(manifestBody(newPkg, newChecksum)))
            server.enqueue(MockResponse().setBody(String(newBytes)))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(listOf(newPkg.version.id), (result as SyncResult.Completed).updatedVersionIds)
            assertEquals(newPkg.version.id, database.amaliyahVersionDao().getActiveForVariant(newPkg.variant.id)?.id)
        }

    @Test
    fun manifestHttp500ReturnsRetryableFailure() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertNull(database.amaliyahDao().getBySlug("sample"))
        }

    @Test
    fun manifestNonRetryableHttpStatusReturnsPermanentFailure() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(400))

            val result = manager.sync()

            assertTrue(result is SyncResult.PermanentFailure)
        }

    @Test
    fun packageTimeoutOrHttp500ReturnsRetryableFailure() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            val checksum = ContentChecksum.sha256Hex(bytes)

            server.enqueue(MockResponse().setBody(manifestBody(pkg, checksum)))
            server.enqueue(MockResponse().setResponseCode(500))

            val result = manager.sync()

            assertTrue(result is SyncResult.RetryableFailure)
            assertNull(database.amaliyahVersionDao().getById(pkg.version.id))
        }

    @Test
    fun retryAfterPackageFailureSkipsAlreadyImportedPackages() =
        runTest {
            val okPkg =
                packageFor(versionNumber = 1, amaliyahId = "ok", variantId = "ok-umum", versionId = "ok-umum-v1")
            val brokenPkg =
                packageFor(
                    versionNumber = 1,
                    amaliyahId = "broken",
                    variantId = "broken-umum",
                    versionId = "broken-umum-v1",
                )
            val okBytes = json.encodeToString(okPkg).encodeToByteArray()
            val okChecksum = ContentChecksum.sha256Hex(okBytes)
            val brokenBytes = json.encodeToString(brokenPkg).encodeToByteArray()
            val brokenChecksum = ContentChecksum.sha256Hex(brokenBytes)

            // First attempt: ok's package succeeds, broken's package times out (HTTP 500).
            server.enqueue(
                MockResponse().setBody(twoPackageManifestBody(okPkg, okChecksum, brokenPkg, brokenChecksum)),
            )
            server.enqueue(MockResponse().setBody(String(okBytes)))
            server.enqueue(MockResponse().setResponseCode(500))

            val firstAttempt = manager.sync()
            assertTrue(firstAttempt is SyncResult.RetryableFailure)
            assertEquals(okPkg.version.id, database.amaliyahVersionDao().getActiveForVariant(okPkg.variant.id)?.id)

            // Second attempt (the "retry"): same manifest, broken's package now succeeds. ok is
            // already active in Room, so only broken's package endpoint should be requested again.
            server.enqueue(
                MockResponse().setBody(twoPackageManifestBody(okPkg, okChecksum, brokenPkg, brokenChecksum)),
            )
            server.enqueue(MockResponse().setBody(String(brokenBytes)))
            val requestsBeforeRetry = server.requestCount

            val secondAttempt = manager.sync()

            assertTrue(secondAttempt is SyncResult.Completed)
            val completed = secondAttempt as SyncResult.Completed
            assertEquals(listOf(brokenPkg.version.id), completed.updatedVersionIds)
            assertEquals(listOf(okPkg.version.id), completed.skippedVersionIds)
            // Manifest + broken's package only — ok's package endpoint was never called again.
            assertEquals(requestsBeforeRetry + 2, server.requestCount)
        }

    @Test
    fun permanentlyInvalidPackageIsRejectedWhileAnotherValidPackageStillImports() =
        runTest {
            val okPkg =
                packageFor(versionNumber = 1, amaliyahId = "ok", variantId = "ok-umum", versionId = "ok-umum-v1")
            val invalidPkg =
                packageFor(versionNumber = 1, amaliyahId = "bad", variantId = "bad-umum", versionId = "bad-umum-v1")
            val okBytes = json.encodeToString(okPkg).encodeToByteArray()
            val okChecksum = ContentChecksum.sha256Hex(okBytes)
            val invalidBytes = json.encodeToString(invalidPkg).encodeToByteArray()
            // Manifest declares a checksum that does not match the actually served bytes — a
            // permanent, checksum-mismatch package failure the importer itself rejects.
            val wrongChecksum = "f".repeat(64)

            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path == "/v1/content/manifest" ->
                                MockResponse().setBody(
                                    twoPackageManifestBody(okPkg, okChecksum, invalidPkg, wrongChecksum),
                                )

                            request.path == "/v1/content/packages/${okPkg.version.id}" ->
                                MockResponse().setBody(String(okBytes))

                            request.path == "/v1/content/packages/${invalidPkg.version.id}" ->
                                MockResponse().setBody(String(invalidBytes))

                            else -> MockResponse().setResponseCode(404)
                        }
                }

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            val completed = result as SyncResult.Completed
            assertEquals(listOf(okPkg.version.id), completed.updatedVersionIds)
            assertEquals(listOf(invalidPkg.version.id), completed.rejectedVersionIds)
            assertNull(database.amaliyahVersionDao().getById(invalidPkg.version.id))
        }

    @Test
    fun checksumConflictSameVersionDifferentChecksumIsRejectedWithoutDownloading() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            ContentPackageImporter(database).importPackage(bytes, pkg.version.id, ContentChecksum.sha256Hex(bytes))

            val conflictingChecksum = "f".repeat(64)
            server.enqueue(MockResponse().setBody(manifestBody(pkg, conflictingChecksum)))

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(listOf(pkg.version.id), (result as SyncResult.Completed).rejectedVersionIds)
            // No package request for a same-version checksum conflict — rejected from the manifest alone.
            assertEquals(1, server.requestCount)
        }

    @Test
    fun minimumAppVersionTooHighRejectsPackageWithoutDownloading() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            val checksum = ContentChecksum.sha256Hex(bytes)

            server.enqueue(
                MockResponse().setBody(manifestBody(pkg, checksum, minimumAppVersionCode = Int.MAX_VALUE)),
            )

            val result = manager.sync()

            assertTrue(result is SyncResult.Completed)
            assertEquals(listOf(pkg.version.id), (result as SyncResult.Completed).rejectedVersionIds)
            assertEquals(1, server.requestCount)
        }

    private fun manifestBody(
        pkg: ContentPackageDto,
        checksum: String,
        minimumAppVersionCode: Int = 1,
    ): String =
        """
        {
          "schemaVersion": 1,
          "packages": [${packageEntryJson(pkg, checksum, minimumAppVersionCode)}]
        }
        """.trimIndent()

    private fun twoPackageManifestBody(
        first: ContentPackageDto,
        firstChecksum: String,
        second: ContentPackageDto,
        secondChecksum: String,
    ): String =
        """
        {
          "schemaVersion": 1,
          "packages": [${packageEntryJson(first, firstChecksum)}, ${packageEntryJson(second, secondChecksum)}]
        }
        """.trimIndent()

    private fun packageEntryJson(
        pkg: ContentPackageDto,
        checksum: String,
        minimumAppVersionCode: Int = 1,
    ): String =
        """
        {
          "contentId": "${pkg.amaliyah.id}",
          "variantId": "${pkg.variant.id}",
          "versionId": "${pkg.version.id}",
          "versionNumber": ${pkg.version.versionNumber},
          "checksumSha256": "$checksum",
          "minimumAppVersionCode": $minimumAppVersionCode
        }
        """.trimIndent()

    private fun packageFor(
        versionNumber: Int,
        amaliyahId: String = "sample",
        variantId: String = "sample-umum",
        versionId: String = "$variantId-v$versionNumber",
    ): ContentPackageDto =
        ContentPackageDto(
            schemaVersion = 1,
            amaliyah =
                AmaliyahDto(
                    id = amaliyahId,
                    slug = amaliyahId,
                    titleId = "Sample",
                    titleAr = "[FIXTURE-AR]",
                    category = "AMALIYAH",
                ),
            variant =
                AmaliyahVariantDto(
                    id = variantId,
                    slug = "umum",
                    nameId = "Umum",
                    nameAr = "[FIXTURE-AR]",
                    ownerType = OwnerType.PUBLIC,
                    visibility = Visibility.PUBLIC,
                    isDefault = true,
                ),
            version =
                AmaliyahVersionDto(
                    id = versionId,
                    versionNumber = versionNumber,
                    status = AmaliyahVersionStatus.PUBLISHED,
                    sourceName = "NON-PRODUCTION FIXTURE",
                    sourceReference = "N/A",
                    minimumAppVersionCode = 1,
                ),
            approval =
                ApprovalDto(
                    id = "$versionId-approval",
                    approverName = "NON-PRODUCTION FIXTURE",
                    approverRole = "N/A",
                    approvalDate = "2026-07-25",
                    approvalScope = "N/A",
                    status = ApprovalStatus.PENDING,
                ),
            steps =
                listOf(
                    AmaliyahStepDto(
                        id = "$versionId-step-01",
                        position = 1,
                        stepType = StepType.HEADING,
                        titleId = "Pembukaan",
                    ),
                ),
        )
}
