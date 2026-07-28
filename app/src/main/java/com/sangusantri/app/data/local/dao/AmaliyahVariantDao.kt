package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity

@Dao
interface AmaliyahVariantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AmaliyahVariantEntity)

    @Upsert
    suspend fun upsert(entity: AmaliyahVariantEntity)

    @Query("SELECT * FROM amaliyah_variants WHERE amaliyahId = :amaliyahId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultForAmaliyah(amaliyahId: String): AmaliyahVariantEntity?
}
