package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.NahwuQuizQuestionEntity

@Dao
interface NahwuQuizQuestionDao {
    @Upsert
    suspend fun upsertAll(entities: List<NahwuQuizQuestionEntity>)

    @Query("SELECT * FROM nahwu_quiz_questions WHERE packageId = :packageId ORDER BY `order` ASC")
    suspend fun getByPackageId(packageId: String): List<NahwuQuizQuestionEntity>
}
