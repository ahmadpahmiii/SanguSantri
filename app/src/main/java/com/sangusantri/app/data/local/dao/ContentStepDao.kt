package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.ContentStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentStepDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<ContentStepEntity>)

    @Query("SELECT * FROM content_steps WHERE contentId = :contentId ORDER BY position ASC")
    suspend fun getByContentId(contentId: String): List<ContentStepEntity>

    /**
     * Ids of items whose step text contains [pattern] — catalogue "search by ayah". [pattern] is
     * the already-escaped needle without its wildcards; the caller escapes because `%`/`_` typed
     * into a search box are literal characters, not operators.
     */
    @Query(
        "SELECT DISTINCT contentId FROM content_steps " +
            "WHERE arabicText LIKE '%' || :pattern || '%' ESCAPE '\\' " +
            "OR translation LIKE '%' || :pattern || '%' ESCAPE '\\'",
    )
    fun observeContentIdsMatchingText(pattern: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM content_steps WHERE contentId = :contentId")
    suspend fun countByContentId(contentId: String): Int

    @Query("DELETE FROM content_steps WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)
}
