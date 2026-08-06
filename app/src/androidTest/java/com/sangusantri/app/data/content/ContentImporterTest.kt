package com.sangusantri.app.data.content

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.data.local.entity.StepProgressEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [ContentImporter]'s high-risk behaviours (ADR 0015): fresh import, idempotency,
 * never-downgrade, id/version identity-mismatch rejection, structural-validation rejection, atomic
 * database-failure rollback, atomic version replacement, and the new, more generous progress
 * preservation (surviving step ids keep their progress; only genuinely removed steps are orphaned)
 * — all against a real in-memory Room database, since transaction/rollback behaviour cannot be
 * proven with mocked DAOs.
 */
@RunWith(AndroidJUnit4::class)
class ContentImporterTest {
    private lateinit var database: SanguSantriDatabase
    private lateinit var importer: ContentImporter

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
        importer = ContentImporter(database)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun firstImportIntoEmptyRoomInsertsTheItem() =
        runTest {
            val outcome = importer.importContentFile(item(version = 1), file(version = 1))

            assertTrue(outcome is ContentImportOutcome.Imported)
            assertEquals(1, database.contentStepDao().countByContentId("sample"))
        }

    @Test
    fun reimportingTheSameVersionIsSkippedAsUpToDate() =
        runTest {
            importer.importContentFile(item(version = 1), file(version = 1))

            val second = importer.importContentFile(item(version = 1), file(version = 1))

            assertTrue(second is ContentImportOutcome.SkippedUpToDate)
            assertEquals(1, database.contentStepDao().countByContentId("sample"))
        }

    @Test
    fun lowerVersionNeverDowngradesRoom() =
        runTest {
            importer.importContentFile(item(version = 2), file(version = 2))

            val outcome = importer.importContentFile(item(version = 1), file(version = 1))

            assertTrue(outcome is ContentImportOutcome.SkippedOlderVersion)
            assertEquals(2, database.contentDao().getById("sample")?.version)
        }

    @Test
    fun sameVersionWithDifferentContentIsSkippedAndRoomKeepsTheOriginal() =
        runTest {
            importer.importContentFile(item(version = 1, title = "Original Title"), file(version = 1))

            // Same id and version, but different bytes — ADR 0015 deliberately has no checksum, so
            // version equality alone is authoritative and this is treated as up to date, not a
            // conflict.
            val outcome = importer.importContentFile(item(version = 1, title = "Different Title"), file(version = 1))

            assertTrue(outcome is ContentImportOutcome.SkippedUpToDate)
            assertEquals("Original Title", database.contentDao().getById("sample")?.title)
        }

    @Test
    fun contentFileIdMismatchIsRejectedAndWritesNothing() =
        runTest {
            val mismatchedFile = file(version = 1).copy(id = "different-id")

            val outcome = importer.importContentFile(item(version = 1), mismatchedFile)

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun contentFileVersionMismatchIsRejectedAndWritesNothing() =
        runTest {
            val mismatchedFile = file(version = 2)

            val outcome = importer.importContentFile(item(version = 1), mismatchedFile)

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun invalidStructureRejectsTheItemAndWritesNothing() =
        runTest {
            val emptySteps = file(version = 1).copy(steps = emptyList())

            val outcome = importer.importContentFile(item(version = 1), emptySteps)

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun databaseFailureMidImportRollsBackTheWholeItem() =
        runTest {
            // Pre-seed an unrelated content row owning the step id this item will try to insert,
            // forcing a primary-key conflict partway through the transaction.
            database.contentDao().upsert(
                ContentEntity(
                    id = "other",
                    title = "Other",
                    description = "[FIXTURE]",
                    imageUrl = null,
                    category = null,
                    version = 1,
                    order = 0,
                    isActive = true,
                    sourceName = "[FIXTURE]",
                    sourceUrl = "https://example.invalid/fixture",
                ),
            )
            database.contentStepDao().insertAll(
                listOf(
                    ContentStepEntity(
                        id = "sample-v1-step-01",
                        contentId = "other",
                        position = 1,
                        arabicText = "[FIXTURE-AR]",
                        translation = "[FIXTURE]",
                        repeatTarget = 1,
                    ),
                ),
            )

            val outcome = importer.importContentFile(item(version = 1), file(version = 1))

            assertTrue(outcome is ContentImportOutcome.Rejected)
            assertNull(database.contentDao().getById("sample"))
        }

    @Test
    fun higherVersionReplacesTheActiveItemAtomically() =
        runTest {
            importer.importContentFile(item(version = 1), file(version = 1))

            val outcome =
                importer.importContentFile(item(version = 2, title = "Updated Title"), file(version = 2))

            assertTrue(outcome is ContentImportOutcome.Replaced)
            assertEquals(1, database.contentStepDao().countByContentId("sample"))
            assertEquals("Updated Title", database.contentDao().getById("sample")?.title)
        }

    @Test
    fun replacingWithADifferentStepIdOrphansItsProgressAndResetsReadingPosition() =
        runTest {
            importer.importContentFile(item(version = 1), file(version = 1))
            seedProgress(stepId = "sample-v1-step-01")

            importer.importContentFile(item(version = 2), file(version = 2))

            assertNull(database.readingPositionDao().getByContentId("sample"))
            assertNull(database.guidedReadingSessionDao().getByContentId("sample"))
            assertTrue(database.stepProgressDao().getByContentId("sample").isEmpty())
        }

    @Test
    fun replacingButReusingAStepIdPreservesItsProgress() =
        runTest {
            importer.importContentFile(item(version = 1), file(version = 1, stepId = "shared-step"))
            seedProgress(stepId = "shared-step")

            importer.importContentFile(item(version = 2), file(version = 2, stepId = "shared-step"))

            assertNull(database.readingPositionDao().getByContentId("sample"))
            assertEquals("shared-step", database.guidedReadingSessionDao().getByContentId("sample")?.currentStepId)
            assertEquals(1, database.stepProgressDao().getByContentId("sample").size)
        }

    private suspend fun seedProgress(stepId: String) {
        database.readingPositionDao().upsert(ReadingPositionEntity("sample", 0, 10, 1_000L))
        database.guidedReadingSessionDao().upsert(GuidedReadingSessionEntity("sample", stepId, 1_000L, null, 1_000L))
        database.stepProgressDao().upsert(StepProgressEntity("sample", stepId, 2, 1_000L))
    }

    private fun item(
        version: Int,
        title: String = "Sample",
    ) = ContentCatalogItemDto(
        id = "sample",
        title = title,
        description = "[FIXTURE] Sample",
        imageUrl = null,
        category = "Tahlil dan Doa",
        version = version,
        contentUrl = "/content/packages/sample-v$version.json",
        order = 1,
        isActive = true,
    )

    private fun file(
        version: Int,
        stepId: String = "sample-v$version-step-01",
    ) = ContentFileDto(
        schemaVersion = ContentValidator.SUPPORTED_SCHEMA_VERSION,
        id = "sample",
        version = version,
        sourceName = "NON-PRODUCTION FIXTURE",
        sourceUrl = "https://example.invalid/fixture",
        steps =
            listOf(
                ContentStepDto(id = stepId, arabicText = "[FIXTURE-AR]", translation = "[FIXTURE]", repeatTarget = 1),
            ),
    )
}
