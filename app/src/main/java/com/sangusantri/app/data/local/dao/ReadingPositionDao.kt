package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.ReadingPositionEntity

@Dao
interface ReadingPositionDao {
    @Upsert
    suspend fun upsert(entity: ReadingPositionEntity)

    @Query("SELECT * FROM reading_positions WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): ReadingPositionEntity?

    @Query("SELECT * FROM reading_positions ORDER BY lastOpenedAtEpochMillis DESC LIMIT 1")
    suspend fun getMostRecent(): ReadingPositionEntity?

    @Query("DELETE FROM reading_positions WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)
}
