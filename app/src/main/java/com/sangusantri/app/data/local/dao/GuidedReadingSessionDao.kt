package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity

@Dao
interface GuidedReadingSessionDao {
    @Upsert
    suspend fun upsert(entity: GuidedReadingSessionEntity)

    @Query("SELECT * FROM guided_reading_sessions WHERE versionId = :versionId")
    suspend fun getByVersionId(versionId: String): GuidedReadingSessionEntity?
}
