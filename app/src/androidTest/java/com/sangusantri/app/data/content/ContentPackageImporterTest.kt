package com.sangusantri.app.data.content

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.dto.AmaliyahDto
import com.sangusantri.app.data.content.dto.AmaliyahStepDto
import com.sangusantri.app.data.content.dto.AmaliyahVariantDto
import com.sangusantri.app.data.content.dto.AmaliyahVersionDto
import com.sangusantri.app.data.content.dto.ApprovalDto
import com.sangusantri.app.data.content.dto.ContentPackageDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.data.local.entity.StepProgressEntity
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the high-risk import/replace behaviours (section 17): fresh import, idempotency,
 * never-downgrade, checksum-conflict rejection, invalid-checksum/structure rejection, atomic
 * database-failure rollback, atomic version replacement, and version-scoped progress reset —
 * all against a real in-memory Room database, since transaction/rollback behaviour cannot be
 * proven with mocked DAOs.
 */
@RunWith(AndroidJUnit4::class)
class ContentPackageImporterTest {
    private lateinit var database: SanguSantriDatabase
    private lateinit var importer: ContentPackageImporter
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
        importer = ContentPackageImporter(database)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun firstImportIntoEmptyRoomInsertsThePackage() =
        runTest {
            val pkg = packageFor(versionNumber = 1)

            val outcome = import(pkg)

            assertTrue(outcome is ContentImportOutcome.Imported)
            assertEquals(1, database.amaliyahStepDao().countByVersionId(pkg.version.id))
        }

    @Test
    fun reimportingTheSamePackageIsIdempotent() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            import(pkg)

            val second = import(pkg)

            assertTrue(second is ContentImportOutcome.AlreadyUpToDate)
            assertEquals(1, database.amaliyahStepDao().countByVersionId(pkg.version.id))
        }

    @Test
    fun lowerVersionNeverDowngradesRoom() =
        runTest {
            val v2 = packageFor(versionNumber = 2, versionId = "sample-umum-v2")
            import(v2)

            val v1 = packageFor(versionNumber = 1, versionId = "sample-umum-v1")
            val outcome = import(v1)

            assertTrue(outcome is ContentImportOutcome.SkippedOlderVersion)
            assertEquals(v2.version.id, database.amaliyahVersionDao().getActiveForVariant("sample-umum")?.id)
        }

    @Test
    fun sameVersionWithDifferentChecksumIsRejectedAsConflictAndRoomIsUnchanged() =
        runTest {
            val v1 = packageFor(versionNumber = 1, titleId = "Original Title")
            import(v1)

            // Same versionNumber and id, but different content bytes (checksum will differ).
            val conflicting = v1.copy(amaliyah = v1.amaliyah.copy(titleId = "Different Title"))
            val outcome = import(conflicting)

            assertTrue(outcome is ContentImportOutcome.ChecksumConflict)
            assertEquals("Original Title", database.amaliyahDao().getBySlug(v1.amaliyah.slug)?.titleId)
        }

    @Test
    fun invalidChecksumRejectsThePackageAndWritesNothing() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            val bytes = json.encodeToString(pkg).encodeToByteArray()

            val outcome = importer.importPackage(bytes, pkg.version.id, "0".repeat(64))

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.amaliyahVersionDao().getById(pkg.version.id))
        }

    @Test
    fun invalidStructureRejectsThePackageAndWritesNothing() =
        runTest {
            val pkg = packageFor(versionNumber = 1).copy(steps = emptyList())
            val bytes = json.encodeToString(pkg).encodeToByteArray()
            val checksum = ContentChecksum.sha256Hex(bytes)

            val outcome = importer.importPackage(bytes, pkg.version.id, checksum)

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.amaliyahDao().getBySlug(pkg.amaliyah.slug))
        }

    @Test
    fun databaseFailureMidImportRollsBackTheWholePackage() =
        runTest {
            val pkg = packageFor(versionNumber = 1)
            // Pre-seed an unrelated row occupying the step id this package will try to insert,
            // forcing a primary-key conflict partway through the transaction.
            seedConflictingStepId(pkg.steps.first().id)

            val outcome = import(pkg)

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.amaliyahDao().getBySlug(pkg.amaliyah.slug))
            assertNull(database.amaliyahVersionDao().getById(pkg.version.id))
        }

    @Test
    fun higherVersionReplacesTheActiveVersionAtomically() =
        runTest {
            val v1 = packageFor(versionNumber = 1, versionId = "sample-umum-v1")
            import(v1)
            val v2 = packageFor(versionNumber = 2, versionId = "sample-umum-v2", titleId = "Updated Title")

            val outcome = import(v2)

            assertTrue(outcome is ContentImportOutcome.Replaced)
            assertNull(database.amaliyahVersionDao().getById(v1.version.id))
            assertEquals(0, database.amaliyahStepDao().countByVersionId(v1.version.id))
            assertEquals(1, database.amaliyahStepDao().countByVersionId(v2.version.id))
            assertEquals("Updated Title", database.amaliyahDao().getBySlug(v1.amaliyah.slug)?.titleId)
        }

    @Test
    fun replacingAVersionRemovesItsVersionScopedProgress() =
        runTest {
            val v1 = packageFor(versionNumber = 1, versionId = "sample-umum-v1")
            import(v1)
            database.readingPositionDao().upsert(ReadingPositionEntity(v1.version.id, 3, 10, 1_000L))
            database
                .guidedReadingSessionDao()
                .upsert(GuidedReadingSessionEntity(v1.version.id, v1.steps.first().id, 1_000L, null, 1_000L))
            database.stepProgressDao().upsert(StepProgressEntity(v1.version.id, v1.steps.first().id, 2, 1_000L))

            val v2 = packageFor(versionNumber = 2, versionId = "sample-umum-v2")
            import(v2)

            assertNull(database.readingPositionDao().getByVersionId(v1.version.id))
            assertNull(database.guidedReadingSessionDao().getByVersionId(v1.version.id))
            assertTrue(database.stepProgressDao().getByVersionId(v1.version.id).isEmpty())
        }

    private suspend fun import(pkg: ContentPackageDto): ContentImportOutcome {
        val bytes = json.encodeToString(pkg).encodeToByteArray()
        val checksum = ContentChecksum.sha256Hex(bytes)
        return importer.importPackage(bytes, pkg.version.id, checksum)
    }

    /** Inserts an unrelated version to own a pre-existing step row, forcing a PK conflict later. */
    private suspend fun seedConflictingStepId(id: String) {
        database.amaliyahDao().insert(
            AmaliyahEntity(
                id = "other",
                slug = "other",
                titleId = "Other",
                titleAr = "[FIXTURE-AR]",
                descriptionId = null,
                descriptionAr = null,
                category = "AMALIYAH",
            ),
        )
        database.amaliyahVariantDao().insert(
            AmaliyahVariantEntity(
                id = "other-umum",
                amaliyahId = "other",
                slug = "umum",
                nameId = "Umum",
                nameAr = "[FIXTURE-AR]",
                ownerType = OwnerType.PUBLIC,
                pondokId = null,
                visibility = Visibility.PUBLIC,
                isDefault = true,
            ),
        )
        database.approvalDao().insert(
            ApprovalEntity(
                id = "other-approval",
                approverName = "NON-PRODUCTION FIXTURE",
                approverRole = "N/A",
                institutionName = null,
                approvalDate = "2026-07-25",
                approvalScope = "N/A",
                publicDocumentStorageKey = null,
                documentReferenceNumber = null,
                status = ApprovalStatus.PENDING,
            ),
        )
        database.amaliyahVersionDao().insert(
            AmaliyahVersionEntity(
                id = "other-v1",
                variantId = "other-umum",
                versionNumber = 1,
                schemaVersion = 1,
                status = AmaliyahVersionStatus.PUBLISHED,
                sourceName = "NON-PRODUCTION FIXTURE",
                sourceReference = "N/A",
                approvalId = "other-approval",
                checksumSha256 = "0".repeat(64),
                minimumAppVersionCode = 1,
                publishedAt = null,
                revokedAt = null,
            ),
        )
        database.amaliyahStepDao().insertAll(
            listOf(
                AmaliyahStepEntity(
                    id = id,
                    versionId = "other-v1",
                    position = 1,
                    stepType = StepType.DIVIDER,
                    titleId = null,
                    titleAr = null,
                    arabicText = null,
                    translationId = null,
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
            ),
        )
    }

    private fun packageFor(
        versionNumber: Int,
        versionId: String = "sample-umum-v$versionNumber",
        titleId: String = "Sample",
    ): ContentPackageDto =
        ContentPackageDto(
            schemaVersion = ContentPackageValidator.SUPPORTED_SCHEMA_VERSION,
            amaliyah =
                AmaliyahDto(
                    id = "sample",
                    slug = "sample",
                    titleId = titleId,
                    titleAr = "[FIXTURE-AR]",
                    category = "AMALIYAH",
                ),
            variant =
                AmaliyahVariantDto(
                    id = "sample-umum",
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
