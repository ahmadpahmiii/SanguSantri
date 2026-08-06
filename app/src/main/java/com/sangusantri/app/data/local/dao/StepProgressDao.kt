package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.StepProgressEntity

@Dao
interface StepProgressDao {
    @Upsert
    suspend fun upsert(entity: StepProgressEntity)

    @Query("SELECT * FROM step_progress WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): List<StepProgressEntity>

    @Query("DELETE FROM step_progress WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)

    // Orphan cleanup after a content update: keep progress only for step ids that still exist.
    @Query("DELETE FROM step_progress WHERE contentId = :contentId AND stepId NOT IN (:survivingStepIds)")
    suspend fun deleteOrphaned(
        contentId: String,
        survivingStepIds: List<String>,
    )
}
