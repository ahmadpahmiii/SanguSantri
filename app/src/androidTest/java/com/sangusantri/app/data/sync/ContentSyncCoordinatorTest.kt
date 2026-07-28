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
import com.sangusantri.app.data.remote.ContentRemoteDataSource
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
 * Exercises [ContentSyncCoordinator] against a real HTTP stack (MockWebServer) and a real Room
 * database, covering the high-risk remote-sync behaviours (section 17): a `304` response never
 * downloads any package, a manifest-fetch failure leaves Room untouched, a package already
 * matching Room's active version/checksum is skipped without a download, and one package's
 * failure is isolated from another package's success in the same manifest.
 */
@RunWith(AndroidJUnit4::class)
class ContentSyncCoordinatorTest {
    private lateinit var server: MockWebServer
    private lateinit var database: SanguSantriDatabase
    private lateinit var coordinator: ContentSyncCoordinator
    private lateinit var syncMetadata: ContentSyncMetadata
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
        val remoteDataSource = ContentRemoteDataSource(api, ApplicationProvider.getApplicationContext())
        val importer = ContentPackageImporter(database)
        syncMetadata = ContentSyncMetadata(database.appMetadataDao())
        coordinator = ContentSyncCoordinator(remoteDataSource, importer, syncMetadata)
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun notModifiedResponseNeverDownloadsAnyPackage() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(304))

            val outcome = coordinator.sync()

            assertTrue(outcome is ContentSyncOutcome.NotModified)
            assertEquals(1, server.requestCount)
        }

    @Test
    fun manifestFetchFailureLeavesRoomUnchanged() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500))

            val outcome = coordinator.sync()

            assertTrue(outcome is ContentSyncOutcome.Failed)
            assertNull(database.amaliyahDao().getBySlug("sample"))
        }

    @Test
    fun packageAlreadyMatchingRoomIsSkippedWithoutDownloading() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            val checksum = ContentChecksum.sha256Hex(bytes)
            ContentPackageImporter(database).importPackage(bytes, pkg.version.id, checksum)

            server.enqueue(MockResponse().setBody(manifestBody(pkg, checksum)))

            val outcome = coordinator.sync()

            assertTrue(outcome is ContentSyncOutcome.NoChanges)
            // Only the manifest request — no package endpoint call for an up-to-date package.
            assertEquals(1, server.requestCount)
        }

    @Test
    fun onePackageFailureIsIsolatedFromAnotherPackagesSuccessInTheSameManifest() =
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
            val brokenChecksum = ContentChecksum.sha256Hex(json.encodeToString(brokenPkg).encodeToByteArray())

            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path == "/v1/content/manifest" ->
                                MockResponse().setBody(
                                    twoPackageManifestBody(okPkg, okChecksum, brokenPkg, brokenChecksum),
                                )

                            request.path == "/v1/content/packages/${okPkg.version.id}" ->
                                MockResponse().setBody(String(okBytes))

                            request.path == "/v1/content/packages/${brokenPkg.version.id}" ->
                                MockResponse().setResponseCode(500)

                            else -> MockResponse().setResponseCode(404)
                        }
                }

            val outcome = coordinator.sync()

            assertTrue(outcome is ContentSyncOutcome.PartialFailure)
            val partial = outcome as ContentSyncOutcome.PartialFailure
            assertEquals(listOf(okPkg.version.id), partial.updatedVersionIds)
            assertEquals(listOf(brokenPkg.version.id), partial.failedVersionIds)
            assertEquals(1, database.amaliyahStepDao().countByVersionId(okPkg.version.id))
            assertNull(database.amaliyahVersionDao().getById(brokenPkg.version.id))
        }

    private fun manifestBody(
        pkg: ContentPackageDto,
        checksum: String,
    ): String =
        """
        {
          "manifestVersion": 1,
          "schemaVersion": 1,
          "generatedAt": "2026-07-28T00:00:00Z",
          "packages": [${packageEntryJson(pkg, checksum)}]
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
          "manifestVersion": 1,
          "schemaVersion": 1,
          "generatedAt": "2026-07-28T00:00:00Z",
          "packages": [${packageEntryJson(first, firstChecksum)}, ${packageEntryJson(second, secondChecksum)}]
        }
        """.trimIndent()

    private fun packageEntryJson(
        pkg: ContentPackageDto,
        checksum: String,
    ): String =
        """
        {
          "contentId": "${pkg.amaliyah.id}",
          "variantId": "${pkg.variant.id}",
          "versionId": "${pkg.version.id}",
          "versionNumber": ${pkg.version.versionNumber},
          "checksumSha256": "$checksum",
          "minimumAppVersionCode": 1,
          "status": "PUBLISHED"
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
