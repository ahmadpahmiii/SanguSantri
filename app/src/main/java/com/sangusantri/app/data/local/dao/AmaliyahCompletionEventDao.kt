package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AmaliyahCompletionEventDao {
    @Insert
    suspend fun insert(entity: AmaliyahCompletionEventEntity)

    @Query("SELECT * FROM amaliyah_completion_events ORDER BY completedAtEpochMillis DESC")
    fun observeAll(): Flow<List<AmaliyahCompletionEventEntity>>
}
