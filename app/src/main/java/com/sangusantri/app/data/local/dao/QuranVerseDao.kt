package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranVerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranVerseDao {
    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surahNumber ORDER BY ayatNumber ASC")
    fun observeBySurah(surahNumber: Int): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE page = :page ORDER BY surahNumber ASC, ayatNumber ASC")
    fun observeByPage(page: Int): Flow<List<QuranVerseEntity>>

    /** The first locally ordered verse of every Juz (QUR-FR-007) — one row per Juz 1..30, derived
     * only from locally stored `juz` fields, never a hardcoded or AI-derived mapping. */
    @Query(
        """
        SELECT * FROM quran_verses AS v
        WHERE NOT EXISTS (
            SELECT 1 FROM quran_verses AS earlier
            WHERE earlier.juz = v.juz
            AND (earlier.surahNumber < v.surahNumber
                OR (earlier.surahNumber = v.surahNumber AND earlier.ayatNumber < v.ayatNumber))
        )
        ORDER BY v.juz ASC
        """,
    )
    fun observeJuzStarts(): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surahNumber AND ayatNumber = :ayatNumber")
    suspend fun getByIdentity(
        surahNumber: Int,
        ayatNumber: Int,
    ): QuranVerseEntity?

    @Query("SELECT COUNT(*) FROM quran_verses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(verses: List<QuranVerseEntity>)

    @Query("DELETE FROM quran_verses")
    suspend fun deleteAll()
}
