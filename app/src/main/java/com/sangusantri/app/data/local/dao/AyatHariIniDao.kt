package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sangusantri.app.data.local.entity.AyatHariIniEntity

@Dao
interface AyatHariIniDao {
    @Query("SELECT * FROM ayat_hari_ini WHERE epochDay = :epochDay")
    suspend fun getByEpochDay(epochDay: Long): AyatHariIniEntity?

    /**
     * The newest published day that is not in the future — today's entry when there is one, and
     * otherwise the most recent one before it.
     *
     * This is what makes a new day survive with no network: the schedule is published as a window,
     * so a device that has synced at all holds days ahead; and a device that has gone past the end
     * of its cached window keeps showing the last ayat it was given rather than an empty header.
     */
    @Query("SELECT * FROM ayat_hari_ini WHERE epochDay <= :epochDay ORDER BY epochDay DESC LIMIT 1")
    suspend fun getLatestOnOrBefore(epochDay: Long): AyatHariIniEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<AyatHariIniEntity>)

    @Query("DELETE FROM ayat_hari_ini WHERE epochDay < :epochDay")
    suspend fun deleteBefore(epochDay: Long)

    /**
     * Replaces the published window in one transaction, then prunes days older than the retention
     * cut-off.
     *
     * A replace rather than a merge: the CMS is the authority on what is scheduled, so an entry the
     * editor deleted must disappear here too. Some history is kept rather than everything past
     * being dropped, because it is the material [getLatestOnOrBefore] falls back to when a sync
     * fails on a new day.
     */
    @Transaction
    suspend fun replaceFrom(
        entries: List<AyatHariIniEntity>,
        pruneBeforeEpochDay: Long,
    ) {
        insertAll(entries)
        deleteBefore(pruneBeforeEpochDay)
    }
}
