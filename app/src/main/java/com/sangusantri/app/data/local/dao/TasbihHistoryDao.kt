package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihHistoryDao {
    @Insert
    suspend fun insert(entity: TasbihHistoryEntity)

    @Query("SELECT * FROM tasbih_history ORDER BY endedAtEpochMillis DESC")
    fun observeAll(): Flow<List<TasbihHistoryEntity>>
}
