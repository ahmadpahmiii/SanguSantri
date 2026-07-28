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

    @Query("SELECT EXISTS(SELECT 1 FROM amaliyah_versions WHERE id = :id)")
    suspend fun existsById(id: String): Boolean

    @Query("SELECT * FROM amaliyah_versions WHERE id = :id")
    suspend fun getById(id: String): AmaliyahVersionEntity?

    @Query(
        "SELECT * FROM amaliyah_versions WHERE variantId = :variantId " +
            "AND status = 'PUBLISHED' ORDER BY versionNumber DESC LIMIT 1",
    )
    suspend fun getLatestPublishedForVariant(variantId: String): AmaliyahVersionEntity?

    // Debug-build-only fallback (ContentRepositoryImpl) so local DRAFT content is visible during
    // development without ever letting a release build treat DRAFT as approved (CLAUDE.md).
    @Query(
        "SELECT * FROM amaliyah_versions WHERE variantId = :variantId " +
            "AND status != 'REVOKED' ORDER BY versionNumber DESC LIMIT 1",
    )
    suspend fun getLatestNonRevokedForVariant(variantId: String): AmaliyahVersionEntity?
}
