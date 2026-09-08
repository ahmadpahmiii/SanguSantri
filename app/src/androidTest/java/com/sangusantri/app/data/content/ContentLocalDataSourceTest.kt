package com.sangusantri.app.data.content

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers [ContentLocalDataSource]'s high-risk behaviours: a list entry making an item visible without
 * steps, a detail filling them in, unchanged-detail idempotency, structural-validation rejection,
 * atomic database-failure rollback, atomic step replacement, and the generous progress
 * preservation (surviving step ids keep their progress; only genuinely removed steps are orphaned)
 * — all against a real in-memory Room database, since transaction/rollback behaviour cannot be
 * proven with mocked DAOs.
 */
@RunWith(AndroidJUnit4::class)
class ContentLocalDataSourceTest {
    private lateinit var database: SanguSantriDatabase
    private lateinit var importer: ContentLocalDataSource

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
        importer = ContentLocalDataSource(database)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun listItemMakesTheItemVisibleWithoutSteps() = runTest {
        val outcome = importer.saveListItem(listItem())

        assertTrue(outcome is ContentWriteOutcome.Imported)
        assertTrue(database.contentDao().getById("sample")!!.isActive)
        assertEquals(0, database.contentStepDao().countByContentId("sample"))
    }

    @Test
    fun detailFillsInTheStepsOfAnItemTheListAlreadyCreated() = runTest {
        importer.saveListItem(listItem())

        val outcome = importer.saveDetail(detail())

        assertTrue(outcome is ContentWriteOutcome.Replaced)
        assertEquals(1, database.contentStepDao().countByContentId("sample"))
    }

    @Test
    fun detailAloneImportsAnItemTheListWasNeverSeenFor() = runTest {
        val outcome = importer.saveDetail(detail())

        assertTrue(outcome is ContentWriteOutcome.Imported)
        assertEquals(1, database.contentStepDao().countByContentId("sample"))
    }

    @Test
    fun reimportingAnUnchangedDetailIsSkippedAsUpToDate() = runTest {
        importer.saveDetail(detail())

        val second = importer.saveDetail(detail())

        assertTrue(second is ContentWriteOutcome.SkippedUpToDate)
        assertEquals(1, database.contentStepDao().countByContentId("sample"))
    }

    @Test
    fun unchangedDetailStillRefreshesMetadata() = runTest {
        importer.saveDetail(detail())

        importer.saveDetail(detail(title = "Corrected Title"))

        assertEquals("Corrected Title", database.contentDao().getById("sample")?.title)
    }

    @Test
    fun listImportNeverOverwritesStepsOrSourceOfAnItemTheDetailAlreadyFilled() = runTest {
        importer.saveDetail(detail())

        importer.saveListItem(listItem(title = "List Title"))

        assertEquals(1, database.contentStepDao().countByContentId("sample"))
        assertEquals("NON-PRODUCTION FIXTURE", database.contentDao().getById("sample")?.sourceName)
    }

    @Test
    fun invalidStructureRejectsTheItemAndWritesNothing() = runTest {
        val outcome = importer.saveDetail(detail().copy(steps = emptyList()))

        assertTrue(outcome is ContentWriteOutcome.Rejected)
        assertNull(database.contentDao().getById("sample"))
    }

    @Test
    fun databaseFailureMidImportRollsBackTheWholeItem() = runTest {
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
                    id = "sample-step-01",
                    contentId = "other",
                    position = 1,
                    arabicText = "[FIXTURE-AR]",
                    translation = "[FIXTURE]",
                    repeatTarget = 1,
                ),
            ),
        )

        val outcome = importer.saveDetail(detail())

        assertTrue(outcome is ContentWriteOutcome.Rejected)
        assertNull(database.contentDao().getById("sample"))
    }

    @Test
    fun changedStepsReplaceTheActiveItemAtomically() = runTest {
        importer.saveDetail(detail())

        val outcome = importer.saveDetail(detail(title = "Updated Title", stepId = "sample-step-02"))

        assertTrue(outcome is ContentWriteOutcome.Replaced)
        assertEquals(1, database.contentStepDao().countByContentId("sample"))
        assertEquals("Updated Title", database.contentDao().getById("sample")?.title)
    }

    @Test
    fun replacingWithADifferentStepIdOrphansItsProgressAndResetsReadingPosition() = runTest {
        importer.saveDetail(detail())
        seedProgress(stepId = "sample-step-01")

        importer.saveDetail(detail(stepId = "sample-step-02"))

        assertNull(database.readingPositionDao().getByContentId("sample"))
        assertNull(database.guidedReadingSessionDao().getByContentId("sample"))
        assertTrue(database.stepProgressDao().getByContentId("sample").isEmpty())
    }

    @Test
    fun replacingButReusingAStepIdPreservesItsProgress() = runTest {
        importer.saveDetail(detail(stepId = "shared-step"))
        seedProgress(stepId = "shared-step")

        importer.saveDetail(detail(stepId = "shared-step", translation = "[FIXTURE] corrected"))

        assertNull(database.readingPositionDao().getByContentId("sample"))
        assertEquals("shared-step", database.guidedReadingSessionDao().getByContentId("sample")?.currentStepId)
        assertEquals(1, database.stepProgressDao().getByContentId("sample").size)
    }

    @Test
    fun deactivateAbsentHidesItemsTheCmsStoppedPublishing() = runTest {
        importer.saveDetail(detail())

        importer.deactivateAbsent(listOf("something-else"))

        assertFalse(database.contentDao().getById("sample")!!.isActive)
    }

    @Test
    fun deactivateAbsentRefusesAnEmptyPublishedSet() = runTest {
        importer.saveDetail(detail())

        assertEquals(0, importer.deactivateAbsent(emptyList()))
        assertTrue(database.contentDao().getById("sample")!!.isActive)
    }

    private suspend fun seedProgress(stepId: String) {
        database.readingPositionDao().upsert(ReadingPositionEntity("sample", 0, 10, 1_000L))
        database.guidedReadingSessionDao().upsert(GuidedReadingSessionEntity("sample", stepId, 1_000L, null, 1_000L))
        database.stepProgressDao().upsert(StepProgressEntity("sample", stepId, 2, 1_000L))
    }

    private fun listItem(title: String = "Sample") = ContentListItemDto(
        id = "sample",
        title = title,
        description = "[FIXTURE] Sample",
        imageUrl = null,
        category = "Amaliyah",
        order = 1,
    )

    private fun detail(
        title: String = "Sample",
        stepId: String = "sample-step-01",
        translation: String = "[FIXTURE]",
    ) = ContentDetailDto(
        schemaVersion = ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION,
        id = "sample",
        title = title,
        description = "[FIXTURE] Sample",
        imageUrl = null,
        category = "Amaliyah",
        order = 1,
        sourceName = "NON-PRODUCTION FIXTURE",
        sourceUrl = "https://example.invalid/fixture",
        steps =
            listOf(
                ContentStepDto(
                    id = stepId,
                    arabicText = "[FIXTURE-AR]",
                    translation = translation,
                    repeatTarget = 1,
                ),
            ),
    )
}
