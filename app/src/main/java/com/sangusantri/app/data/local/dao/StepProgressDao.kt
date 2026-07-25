package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.StepProgressEntity

@Dao
interface StepProgressDao {
    @Upsert
    suspend fun upsert(entity: StepProgressEntity)

    @Query("SELECT * FROM step_progress WHERE versionId = :versionId")
    suspend fun getByVersionId(versionId: String): List<StepProgressEntity>
}
