package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity

@Dao
interface AmaliyahVersionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AmaliyahVersionEntity)

    @Query("SELECT * FROM amaliyah_versions WHERE id = :id")
    suspend fun getById(id: String): AmaliyahVersionEntity?

    @Query(
        "SELECT * FROM amaliyah_versions WHERE variantId = :variantId " +
            "AND status = 'PUBLISHED' ORDER BY versionNumber DESC LIMIT 1",
    )
    suspend fun getLatestPublishedForVariant(variantId: String): AmaliyahVersionEntity?

    // Android retains only one active version per variant (no previous-version fallback) — this is
    // that row, regardless of status, used by ContentPackageImporter to decide import/replace/skip.
    @Query("SELECT * FROM amaliyah_versions WHERE variantId = :variantId ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getActiveForVariant(variantId: String): AmaliyahVersionEntity?

    @Query("DELETE FROM amaliyah_versions WHERE id = :id")
    suspend fun deleteById(id: String)
}
