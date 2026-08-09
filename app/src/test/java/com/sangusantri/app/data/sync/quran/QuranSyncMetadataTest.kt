package com.sangusantri.app.data.sync.quran

import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranSyncMetadataTest {
    @Test
    fun missingVersionCanAdoptTheExistingDatasetBaseline() =
        runTest {
            val metadata = QuranSyncMetadata(FakeAppMetadataDao())

            assertNull(metadata.getAppliedStableVersion())
            assertEquals(1, metadata.adoptStableVersion(1))
            assertEquals(1, metadata.getAppliedStableVersion())
        }

    @Test
    fun onlyANewerTargetIsEligible() =
        runTest {
            val metadata = QuranSyncMetadata(FakeAppMetadataDao())

            assertFalse(metadata.isUpdateAttemptEligible(appliedVersion = 2, targetVersion = 1))
            assertFalse(metadata.isUpdateAttemptEligible(appliedVersion = 2, targetVersion = 2))
            assertTrue(metadata.isUpdateAttemptEligible(appliedVersion = 2, targetVersion = 3))
        }

    @Test
    fun failedTargetIsCooledDownButANewerTargetCanProceedImmediately() =
        runTest {
            val attemptAt = 10_000L
            val dao = FakeAppMetadataDao()
            dao.upsert(
                AppMetadataEntity(
                    QuranSyncMetadata.KEY_LAST_FAILED_UPDATE_ATTEMPT,
                    "2",
                    attemptAt,
                ),
            )
            val metadata = QuranSyncMetadata(dao)

            assertFalse(
                metadata.isUpdateAttemptEligible(
                    appliedVersion = 1,
                    targetVersion = 2,
                    nowEpochMillis = attemptAt + QuranSyncMetadata.FAILED_UPDATE_COOLDOWN_MILLIS - 1,
                ),
            )
            assertTrue(
                metadata.isUpdateAttemptEligible(
                    appliedVersion = 1,
                    targetVersion = 2,
                    nowEpochMillis = attemptAt + QuranSyncMetadata.FAILED_UPDATE_COOLDOWN_MILLIS,
                ),
            )
            assertTrue(
                metadata.isUpdateAttemptEligible(
                    appliedVersion = 1,
                    targetVersion = 3,
                    nowEpochMillis = attemptAt + 1,
                ),
            )
        }

    private class FakeAppMetadataDao : AppMetadataDao {
        private val storage = mutableMapOf<String, AppMetadataEntity>()

        override suspend fun upsert(entity: AppMetadataEntity) {
            storage[entity.key] = entity
        }

        override suspend fun getByKey(key: String): AppMetadataEntity? = storage[key]
    }
}
