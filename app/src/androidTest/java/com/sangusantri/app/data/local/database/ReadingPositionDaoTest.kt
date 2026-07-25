package com.sangusantri.app.data.local.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingPositionDaoTest {
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
    fun insertThenGetByVersionIdReturnsTheStoredPosition() =
        runTest {
            val dao = database.readingPositionDao()
            val entity =
                ReadingPositionEntity(
                    versionId = "v1",
                    itemIndex = 3,
                    itemOffset = 40,
                    lastOpenedAtEpochMillis = 1_000L,
                )

            dao.upsert(entity)
            val result = dao.getByVersionId("v1")

            assertEquals(entity, result)
        }

    @Test
    fun getByVersionIdReturnsNullWhenNoPositionStored() =
        runTest {
            val dao = database.readingPositionDao()

            val result = dao.getByVersionId("missing")

            assertNull(result)
        }

    @Test
    fun upsertReplacesThePositionForTheSameVersion() =
        runTest {
            val dao = database.readingPositionDao()
            dao.upsert(ReadingPositionEntity("v1", 0, 0, 1_000L))

            dao.upsert(ReadingPositionEntity("v1", 5, 200, 2_000L))
            val result = dao.getByVersionId("v1")

            assertEquals(5, result?.itemIndex)
            assertEquals(200, result?.itemOffset)
        }

    @Test
    fun separateContentVersionsKeepIndependentPositions() =
        runTest {
            val dao = database.readingPositionDao()
            dao.upsert(ReadingPositionEntity("v1", 2, 10, 1_000L))
            dao.upsert(ReadingPositionEntity("v2", 7, 90, 1_000L))

            assertEquals(2, dao.getByVersionId("v1")?.itemIndex)
            assertEquals(7, dao.getByVersionId("v2")?.itemIndex)
        }
}
