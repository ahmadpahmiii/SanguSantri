package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Upsert
    suspend fun upsert(entity: ContentEntity)

    @Query("SELECT * FROM content WHERE id = :id")
    suspend fun getById(id: String): ContentEntity?

    @Query("SELECT * FROM content WHERE isActive = 1 ORDER BY `order` ASC")
    fun observeActive(): Flow<List<ContentEntity>>

    @Query("DELETE FROM content WHERE id = :id")
    suspend fun deleteById(id: String)
}
