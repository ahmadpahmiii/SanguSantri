package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.AppMetadataEntity

@Dao
interface AppMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppMetadataEntity)

    @Query("SELECT * FROM app_metadata WHERE `key` = :key")
    suspend fun getByKey(key: String): AppMetadataEntity?
}
