package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.ReadingPositionEntity

@Dao
interface ReadingPositionDao {
    @Upsert
    suspend fun upsert(entity: ReadingPositionEntity)

    @Query("SELECT * FROM reading_positions WHERE versionId = :versionId")
    suspend fun getByVersionId(versionId: String): ReadingPositionEntity?
}
