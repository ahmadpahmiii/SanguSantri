package com.sangusantri.app.data.sync

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ContentSyncMetadataTest {
    @Test
    fun noPriorSyncMeansNullLastSyncTimestamp() =
        runTest {
            val metadata = ContentSyncMetadata(FakeAppMetadataDao())

            assertNull(metadata.getLastSyncAtEpochMillis())
        }

    @Test
    fun recordTerminalSyncPersistsStatusAndTimestamp() =
        runTest {
            val dao = FakeAppMetadataDao()
            val metadata = ContentSyncMetadata(dao)

            metadata.recordTerminalSync(ContentSyncStatus.FAILED)

            assertEquals(ContentSyncStatus.FAILED.name, dao.getByKey(ContentSyncMetadata.KEY_LAST_SYNC)?.value)
            assertNotNull(metadata.getLastSyncAtEpochMillis())
        }

    @Test
    fun recordTerminalSyncOverwritesThePreviousStatus() =
        runTest {
            val dao = FakeAppMetadataDao()
            val metadata = ContentSyncMetadata(dao)

            metadata.recordTerminalSync(ContentSyncStatus.PARTIAL)
            metadata.recordTerminalSync(ContentSyncStatus.SUCCESS)

            assertEquals(ContentSyncStatus.SUCCESS.name, dao.getByKey(ContentSyncMetadata.KEY_LAST_SYNC)?.value)
        }

    private class FakeAppMetadataDao : AppMetadataDao {
        private val storage = mutableMapOf<String, AppMetadataEntity>()

        override suspend fun upsert(entity: AppMetadataEntity) {
            storage[entity.key] = entity
        }

        override suspend fun getByKey(key: String): AppMetadataEntity? = storage[key]
    }
}
