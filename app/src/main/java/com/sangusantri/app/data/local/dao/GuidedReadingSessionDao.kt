package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity

@Dao
interface GuidedReadingSessionDao {
    @Upsert
    suspend fun upsert(entity: GuidedReadingSessionEntity)

    @Query("SELECT * FROM guided_reading_sessions WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): GuidedReadingSessionEntity?

    @Query(
        "SELECT * FROM guided_reading_sessions WHERE completedAtEpochMillis IS NULL " +
            "ORDER BY lastOpenedAtEpochMillis DESC LIMIT 1",
    )
    suspend fun getMostRecentIncomplete(): GuidedReadingSessionEntity?

    @Query("DELETE FROM guided_reading_sessions WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)

    @Query(
        "DELETE FROM guided_reading_sessions WHERE contentId = :contentId AND currentStepId NOT IN (:survivingStepIds)",
    )
    suspend fun deleteIfCurrentStepMissing(
        contentId: String,
        survivingStepIds: List<String>,
    )
}
