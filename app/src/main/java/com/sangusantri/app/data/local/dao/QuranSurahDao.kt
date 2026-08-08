package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranSurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranSurahDao {
    @Query("SELECT * FROM quran_surahs ORDER BY number ASC")
    fun observeAll(): Flow<List<QuranSurahEntity>>

    @Query("SELECT * FROM quran_surahs WHERE number = :number")
    suspend fun getByNumber(number: Int): QuranSurahEntity?

    @Query("SELECT COUNT(*) FROM quran_surahs")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surahs: List<QuranSurahEntity>)

    @Query("DELETE FROM quran_surahs")
    suspend fun deleteAll()
}
