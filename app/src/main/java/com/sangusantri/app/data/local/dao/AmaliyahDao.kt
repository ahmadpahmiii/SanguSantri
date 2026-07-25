package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AmaliyahDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AmaliyahEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM amaliyah WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Query("SELECT * FROM amaliyah ORDER BY slug ASC")
    fun observeAll(): Flow<List<AmaliyahEntity>>

    @Query("SELECT * FROM amaliyah WHERE slug = :slug")
    suspend fun getBySlug(slug: String): AmaliyahEntity?
}
