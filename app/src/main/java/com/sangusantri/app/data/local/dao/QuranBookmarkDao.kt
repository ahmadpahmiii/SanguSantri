package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranBookmarkDao {
    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<QuranBookmarkEntity>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber)",
    )
    fun observeIsBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Flow<Boolean>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber)",
    )
    suspend fun isBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber")
    suspend fun delete(
        surahNumber: Int,
        ayatNumber: Int,
    )
}
