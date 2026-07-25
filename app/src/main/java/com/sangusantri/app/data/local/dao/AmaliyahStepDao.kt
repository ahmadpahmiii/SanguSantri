package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity

@Dao
interface AmaliyahStepDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<AmaliyahStepEntity>)

    @Query("SELECT * FROM amaliyah_steps WHERE versionId = :versionId ORDER BY position ASC")
    suspend fun getByVersionId(versionId: String): List<AmaliyahStepEntity>

    @Query("SELECT COUNT(*) FROM amaliyah_steps WHERE versionId = :versionId")
    suspend fun countByVersionId(versionId: String): Int
}
