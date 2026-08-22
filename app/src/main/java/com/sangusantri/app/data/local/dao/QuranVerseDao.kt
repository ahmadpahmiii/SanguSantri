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

    /**
     * Every ayat on a run of mushaf pages, in reading order across surah boundaries.
     *
     * A halaman is a page of the mushaf, not a slice of one surah: page 603 carries the whole of
     * Al-Kafirun, An-Nasr and Al-Lahab. Ordering by `surahNumber, ayatNumber` within the page keeps
     * them in the order they are printed, so a page can be rendered by walking the rows.
     */
    @Query(
        """
        SELECT * FROM quran_verses
        WHERE page BETWEEN :fromPage AND :toPage
        ORDER BY page ASC, surahNumber ASC, ayatNumber ASC
        """,
    )
    fun observeByPageRange(
        fromPage: Int,
        toPage: Int,
    ): Flow<List<QuranVerseEntity>>

    /**
     * The first locally ordered verse of every Juz (QUR-FR-007) — one row per Juz 1..30, derived
     * only from locally stored `juz` fields, never a hardcoded or AI-derived mapping.
     *
     * The grouped position query is intentional. The former correlated `NOT EXISTS` scanned the
     * 6,236-row verse table once per candidate row and took seconds even though the result has
     * only 30 rows. Surah numbers are 1..114 and ayat numbers are below 1,000, so this numeric
     * position preserves the canonical `(surahNumber, ayatNumber)` ordering without changing
     * Quran content.
     */
    @Query(
        """
        SELECT v.* FROM quran_verses AS v
        INNER JOIN (
            SELECT juz, MIN(surahNumber * 1000 + ayatNumber) AS firstPosition
            FROM quran_verses
            GROUP BY juz
        ) AS juz_start
            ON juz_start.juz = v.juz
            AND juz_start.firstPosition = v.surahNumber * 1000 + v.ayatNumber
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
