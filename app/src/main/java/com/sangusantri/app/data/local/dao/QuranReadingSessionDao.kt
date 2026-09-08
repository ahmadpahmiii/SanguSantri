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

    /**
     * Every (session timestamp, mushaf page) pair a reading session covered — Amalan Harian's
     * "3 halaman" input.
     *
     * A session stores an ayat range; the page it was printed on lives on the ayat, so the two are
     * joined here rather than approximated. `DISTINCT` collapses the many ayat that share a page,
     * leaving at most a handful of rows per session.
     */
    @Query(
        """
        SELECT DISTINCT s.readAtEpochMillis AS readAtEpochMillis, v.page AS page
        FROM quran_reading_sessions s
        JOIN quran_verses v
          ON v.surahNumber = s.surahNumber AND v.ayatNumber BETWEEN s.startAyat AND s.endAyat
        """,
    )
    fun observeSessionPages(): Flow<List<QuranSessionPageRow>>

    @Insert
    suspend fun insert(entity: QuranReadingSessionEntity): Long
}

/** Projection for [QuranReadingSessionDao.observeSessionPages] — not a table. */
data class QuranSessionPageRow(val readAtEpochMillis: Long, val page: Int)
