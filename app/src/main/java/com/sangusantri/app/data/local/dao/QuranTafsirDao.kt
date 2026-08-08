package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.QuranTafsirEntity

@Dao
interface QuranTafsirDao {
    @Query("SELECT * FROM quran_tafsir WHERE remoteAyatId = :remoteAyatId")
    suspend fun getByRemoteAyatId(remoteAyatId: Long): QuranTafsirEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuranTafsirEntity)
}
