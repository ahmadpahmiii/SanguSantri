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

    /**
     * Whether the round that began at [startedAtEpochMillis] is already archived. A round is
     * archived the moment its target is reached, so the later "finish"/"switch target" paths must
     * not file it a second time — and every fresh round stamps a new start time, which is what
     * makes this a safe dedupe key.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM tasbih_history WHERE startedAtEpochMillis = :startedAtEpochMillis)")
    suspend fun isArchived(startedAtEpochMillis: Long): Boolean

    @Query("SELECT * FROM tasbih_history ORDER BY endedAtEpochMillis DESC")
    fun observeAll(): Flow<List<TasbihHistoryEntity>>
}
