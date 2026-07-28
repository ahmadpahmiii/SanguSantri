package com.sangusantri.app.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the scheduling-policy high-risk behaviours (section 17): the 24-hour gate prevents
 * duplicate network work, and unique WorkManager scheduling uses [androidx.work.ExistingWorkPolicy.KEEP].
 */
@RunWith(AndroidJUnit4::class)
class ContentSyncSchedulerTest {
    private lateinit var context: Context
    private lateinit var metadataDao: FakeAppMetadataDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        metadataDao = FakeAppMetadataDao()
    }

    @Test
    fun freshInstallWithNoPriorSyncEnqueuesWork() =
        runTest {
            ContentSyncScheduler(context, ContentSyncMetadata(metadataDao)).enqueueIfStale()

            assertEquals(1, workInfos().size)
        }

    @Test
    fun recentSyncWithinTwentyFourHoursDoesNotEnqueueWork() =
        runTest {
            metadataDao.seedLastSync(System.currentTimeMillis())

            ContentSyncScheduler(context, ContentSyncMetadata(metadataDao)).enqueueIfStale()

            assertTrue(workInfos().isEmpty())
        }

    @Test
    fun staleSyncOlderThanTwentyFourHoursEnqueuesWork() =
        runTest {
            metadataDao.seedLastSync(System.currentTimeMillis() - TWENTY_FIVE_HOURS_MILLIS)

            ContentSyncScheduler(context, ContentSyncMetadata(metadataDao)).enqueueIfStale()

            assertEquals(1, workInfos().size)
        }

    @Test
    fun repeatedCallsUseKeepPolicyAndNeverDuplicateWork() =
        runTest {
            val scheduler = ContentSyncScheduler(context, ContentSyncMetadata(metadataDao))

            scheduler.enqueueIfStale()
            scheduler.enqueueIfStale()

            val infos = workInfos()
            assertEquals(1, infos.size)
            assertTrue(infos.first().state == WorkInfo.State.ENQUEUED)
        }

    private fun workInfos(): List<WorkInfo> =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWork(ContentSyncScheduler.UNIQUE_WORK_NAME)
            .get()

    private class FakeAppMetadataDao : AppMetadataDao {
        private val storage = mutableMapOf<String, AppMetadataEntity>()

        override suspend fun upsert(entity: AppMetadataEntity) {
            storage[entity.key] = entity
        }

        override suspend fun getByKey(key: String): AppMetadataEntity? = storage[key]

        suspend fun seedLastSync(epochMillis: Long) {
            upsert(AppMetadataEntity(ContentSyncMetadata.KEY_LAST_SYNC, ContentSyncStatus.UPDATED.name, epochMillis))
        }
    }

    private companion object {
        const val TWENTY_FIVE_HOURS_MILLIS = 25 * 60 * 60 * 1000L
    }
}
