package com.sangusantri.app.data.local.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SanguSantriDatabaseTest {
    private lateinit var database: SanguSantriDatabase

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SanguSantriDatabase::class.java,
                ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun upsertThenGetByKeyReturnsTheStoredEntity() =
        runTest {
            val dao = database.appMetadataDao()
            val entity = AppMetadataEntity(key = "schema_version", value = "1", updatedAtEpochMillis = 100L)

            dao.upsert(entity)
            val result = dao.getByKey("schema_version")

            assertEquals(entity, result)
        }

    @Test
    fun getByKeyReturnsNullWhenTheKeyIsMissing() =
        runTest {
            val dao = database.appMetadataDao()

            val result = dao.getByKey("missing_key")

            assertNull(result)
        }

    @Test
    fun upsertReplacesTheExistingValueForTheSameKey() =
        runTest {
            val dao = database.appMetadataDao()
            dao.upsert(AppMetadataEntity(key = "schema_version", value = "1", updatedAtEpochMillis = 100L))

            dao.upsert(AppMetadataEntity(key = "schema_version", value = "2", updatedAtEpochMillis = 200L))
            val result = dao.getByKey("schema_version")

            assertEquals("2", result?.value)
        }
}
