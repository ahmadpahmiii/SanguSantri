package com.sangusantri.app.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [MIGRATION_1_2] (ADR 0015) against a real SQLite database — the old
 * Amaliyah/Variant/Version/Step/Approval hierarchy collapsing into the flat `content`/
 * `content_steps` model, with reading/guided/step progress re-keyed from `versionId` to the
 * new content id, is exactly the kind of transformation that cannot be trusted without running
 * it against real rows.
 */
@RunWith(AndroidJUnit4::class)
class SanguSantriMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            SanguSantriDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrate1To2CollapsesTheHierarchyAndPreservesProgress() {
        helper.createDatabase(TEST_DB, 1).apply {
            // A published amaliyah: one HEADING step (dropped by the migration, no Arabic body to
            // carry forward) and two real reading steps (survive, renumbered to a dense 1..N).
            execSQL(
                "INSERT INTO amaliyah (id, slug, titleId, titleAr, descriptionId, descriptionAr, category) " +
                    "VALUES ('tahlil', 'tahlil', 'Tahlil', '[FIXTURE-AR] Tahlil', '[FIXTURE] desc', NULL, 'AMALIYAH')",
            )
            execSQL(
                "INSERT INTO amaliyah_variants " +
                    "(id, amaliyahId, slug, nameId, nameAr, ownerType, pondokId, visibility, isDefault) VALUES " +
                    "('tahlil-umum', 'tahlil', 'umum', 'Umum', '[FIXTURE-AR]', 'PUBLIC', NULL, 'PUBLIC', 1)",
            )
            execSQL(
                "INSERT INTO approvals " +
                    "(id, approverName, approverRole, institutionName, approvalDate, approvalScope, " +
                    "publicDocumentStorageKey, documentReferenceNumber, status) VALUES " +
                    "('approval-1', '[FIXTURE]', '[FIXTURE]', NULL, '2026-01-01', '[FIXTURE]', NULL, NULL, 'APPROVED')",
            )
            execSQL(
                "INSERT INTO amaliyah_versions " +
                    "(id, variantId, versionNumber, schemaVersion, status, sourceName, sourceReference, " +
                    "approvalId, checksumSha256, minimumAppVersionCode, publishedAt, revokedAt) VALUES " +
                    "('tahlil-umum-v1', 'tahlil-umum', 1, 1, 'PUBLISHED', '[FIXTURE] source', '[FIXTURE] ref', " +
                    "'approval-1', 'abc', 1, '2026-01-01T00:00:00Z', NULL)",
            )
            execSQL(
                "INSERT INTO amaliyah_steps " +
                    "(id, versionId, position, stepType, titleId, titleAr, arabicText, translationId, " +
                    "instructionId, instructionAr, repeatTarget, quranSurahNumber, quranAyahStart, " +
                    "quranAyahEnd, audioGroupId) VALUES " +
                    "('step-1', 'tahlil-umum-v1', 1, 'HEADING', 'Pembukaan', NULL, NULL, NULL, " +
                    "NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
            )
            execSQL(
                "INSERT INTO amaliyah_steps " +
                    "(id, versionId, position, stepType, titleId, titleAr, arabicText, translationId, " +
                    "instructionId, instructionAr, repeatTarget, quranSurahNumber, quranAyahStart, " +
                    "quranAyahEnd, audioGroupId) VALUES " +
                    "('step-2', 'tahlil-umum-v1', 2, 'ARABIC_TEXT', NULL, NULL, '[FIXTURE-AR] 2', '[FIXTURE] 2', " +
                    "NULL, NULL, NULL, NULL, NULL, NULL, NULL)",
            )
            execSQL(
                "INSERT INTO amaliyah_steps " +
                    "(id, versionId, position, stepType, titleId, titleAr, arabicText, translationId, " +
                    "instructionId, instructionAr, repeatTarget, quranSurahNumber, quranAyahStart, " +
                    "quranAyahEnd, audioGroupId) VALUES " +
                    "('step-3', 'tahlil-umum-v1', 3, 'PRAYER', NULL, NULL, '[FIXTURE-AR] 3', '[FIXTURE] 3', " +
                    "NULL, NULL, 3, NULL, NULL, NULL, NULL)",
            )

            // Reading/guided/step progress for the published version — guided session and step
            // progress both reference the surviving step-2, and one extra step-progress row
            // references the dropped step-1 to prove orphaned progress is not carried forward.
            execSQL(
                "INSERT INTO reading_positions (versionId, itemIndex, itemOffset, lastOpenedAtEpochMillis) " +
                    "VALUES ('tahlil-umum-v1', 1, 50, 1000)",
            )
            execSQL(
                "INSERT INTO guided_reading_sessions " +
                    "(versionId, currentStepId, lastOpenedAtEpochMillis, completedAtEpochMillis, " +
                    "startedAtEpochMillis) VALUES ('tahlil-umum-v1', 'step-2', 1000, NULL, 500)",
            )
            execSQL(
                "INSERT INTO step_progress (versionId, stepId, currentCount, updatedAtEpochMillis) VALUES " +
                    "('tahlil-umum-v1', 'step-2', 2, 1000)",
            )
            execSQL(
                "INSERT INTO step_progress (versionId, stepId, currentCount, updatedAtEpochMillis) VALUES " +
                    "('tahlil-umum-v1', 'step-1', 1, 1000)",
            )

            // A second amaliyah with only a DRAFT version — must not produce a content row at all
            // (resolvePublishedVersion finds no PUBLISHED version and the amaliyah is skipped).
            execSQL(
                "INSERT INTO amaliyah (id, slug, titleId, titleAr, descriptionId, descriptionAr, category) " +
                    "VALUES ('istighosah', 'istighosah', 'Istighosah', '[FIXTURE-AR]', NULL, NULL, 'AMALIYAH')",
            )
            execSQL(
                "INSERT INTO amaliyah_variants " +
                    "(id, amaliyahId, slug, nameId, nameAr, ownerType, pondokId, visibility, isDefault) VALUES " +
                    "('istighosah-umum', 'istighosah', 'umum', 'Umum', '[FIXTURE-AR]', 'PUBLIC', NULL, 'PUBLIC', 1)",
            )
            execSQL(
                "INSERT INTO amaliyah_versions " +
                    "(id, variantId, versionNumber, schemaVersion, status, sourceName, sourceReference, " +
                    "approvalId, checksumSha256, minimumAppVersionCode, publishedAt, revokedAt) VALUES " +
                    "('istighosah-umum-v1', 'istighosah-umum', 1, 1, 'DRAFT', '[FIXTURE]', '[FIXTURE]', " +
                    "'approval-1', 'def', 1, NULL, NULL)",
            )

            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT id, title, version, sourceName, sourceUrl FROM content").use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals("tahlil", cursor.getString(0))
            assertEquals("Tahlil", cursor.getString(1))
            assertEquals(1, cursor.getInt(2))
            assertEquals("[FIXTURE] source", cursor.getString(3))
            assertEquals("[FIXTURE] ref", cursor.getString(4))
        }

        migrated
            .query("SELECT id, position, arabicText, translation, repeatTarget FROM content_steps ORDER BY position")
            .use { cursor ->
                assertEquals(2, cursor.count)
                assertTrue(cursor.moveToFirst())
                assertEquals("step-2", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
                assertEquals("[FIXTURE-AR] 2", cursor.getString(2))
                assertEquals("[FIXTURE] 2", cursor.getString(3))
                assertEquals(1, cursor.getInt(4))

                assertTrue(cursor.moveToNext())
                assertEquals("step-3", cursor.getString(0))
                assertEquals(2, cursor.getInt(1))
                assertEquals("[FIXTURE-AR] 3", cursor.getString(2))
                assertEquals("[FIXTURE] 3", cursor.getString(3))
                assertEquals(3, cursor.getInt(4))
            }

        migrated.query("SELECT itemIndex, itemOffset FROM reading_positions WHERE contentId = 'tahlil'").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1, c.getInt(0))
            assertEquals(50, c.getInt(1))
        }

        migrated
            .query("SELECT currentStepId, startedAtEpochMillis FROM guided_reading_sessions WHERE contentId = 'tahlil'")
            .use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("step-2", c.getString(0))
                assertEquals(500L, c.getLong(1))
            }

        migrated.query("SELECT stepId, currentCount FROM step_progress WHERE contentId = 'tahlil'").use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals("step-2", cursor.getString(0))
            assertEquals(2, cursor.getInt(1))
        }

        migrated.query("SELECT id FROM content WHERE id = 'istighosah'").use { cursor ->
            assertFalse(cursor.moveToFirst())
        }

        val droppedTables = listOf("amaliyah", "amaliyah_variants", "amaliyah_versions", "amaliyah_steps", "approvals")
        for (droppedTable in droppedTables) {
            migrated
                .query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '$droppedTable'")
                .use { cursor -> assertFalse(cursor.moveToFirst()) }
        }

        migrated.close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
