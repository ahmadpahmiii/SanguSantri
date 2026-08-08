package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranReadingStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranReadingStateDao {
    // QuranReadingStateEntity.SINGLETON_ID inlined: Room's KSP processor requires a compile-time
    // constant query string, and this table only ever holds that one row.
    @Query("SELECT * FROM quran_reading_state WHERE id = 1")
    fun observe(): Flow<QuranReadingStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuranReadingStateEntity)
}
