package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranReadingSessionDao {
    @Query("SELECT * FROM quran_reading_sessions ORDER BY readAtEpochMillis DESC")
    fun observeAll(): Flow<List<QuranReadingSessionEntity>>

    @Insert
    suspend fun insert(entity: QuranReadingSessionEntity): Long
}
