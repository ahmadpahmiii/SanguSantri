package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.TasbihSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbihSessionDao {
    @Upsert
    suspend fun upsert(entity: TasbihSessionEntity)

    @Query("SELECT * FROM tasbih_sessions WHERE id = 0")
    suspend fun get(): TasbihSessionEntity?

    @Query("SELECT * FROM tasbih_sessions WHERE id = 0")
    fun observe(): Flow<TasbihSessionEntity?>

    @Query("DELETE FROM tasbih_sessions WHERE id = 0")
    suspend fun clear()
}
