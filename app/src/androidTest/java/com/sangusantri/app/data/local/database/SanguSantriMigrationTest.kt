package com.sangusantri.app.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun migrate1To2CreatesTheContentHierarchyTablesWithoutDataLoss() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO app_metadata (`key`, `value`, `updatedAtEpochMillis`) VALUES ('k', 'v', 1)",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT `key`, `value` FROM app_metadata").use {
            assertTrue(it.moveToFirst())
            assertEquals("k", it.getString(0))
            assertEquals("v", it.getString(1))
        }

        val newTables =
            listOf("amaliyah", "amaliyah_variants", "approvals", "amaliyah_versions", "amaliyah_steps")
        newTables.forEach { table ->
            migrated.query("SELECT COUNT(*) FROM $table").use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
