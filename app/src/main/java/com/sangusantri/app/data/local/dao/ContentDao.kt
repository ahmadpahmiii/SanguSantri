package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Upsert
    suspend fun upsert(entity: ContentEntity)

    @Query("SELECT * FROM content WHERE id = :id")
    suspend fun getById(id: String): ContentEntity?

    /** The live version of [getById]. Steps and metadata change independently — a title or layout
     * correction touches no step — so the reader observes both and combines them. */
    @Query("SELECT * FROM content WHERE id = :id")
    fun observeById(id: String): Flow<ContentEntity?>

    @Query("SELECT * FROM content WHERE isActive = 1 ORDER BY `order` ASC")
    fun observeActive(): Flow<List<ContentEntity>>

    @Query("DELETE FROM content WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Hides every active item the CMS stopped publishing, and reports how many. Unpublishing is
     * "stop distributing", not "recall": the row and its steps stay in Room, so a reader who is
     * mid-way through an item does not lose it — it just leaves Beranda.
     */
    @Query("UPDATE content SET isActive = 0 WHERE isActive = 1 AND id NOT IN (:publishedIds)")
    suspend fun deactivateAbsent(publishedIds: List<String>): Int
}
