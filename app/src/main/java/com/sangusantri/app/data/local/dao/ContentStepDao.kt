package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.ContentStepEntity

@Dao
interface ContentStepDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<ContentStepEntity>)

    @Query("SELECT * FROM content_steps WHERE contentId = :contentId ORDER BY position ASC")
    suspend fun getByContentId(contentId: String): List<ContentStepEntity>

    @Query("SELECT COUNT(*) FROM content_steps WHERE contentId = :contentId")
    suspend fun countByContentId(contentId: String): Int

    @Query("DELETE FROM content_steps WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)
}
