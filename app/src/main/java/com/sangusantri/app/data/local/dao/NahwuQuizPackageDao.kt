package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.NahwuQuizPackageEntity
import kotlinx.coroutines.flow.Flow

/** One package row plus its bundled question count — [NahwuQuizAttemptDao]'s attempt rows are
 * combined with this in `NahwuQuizRepositoryImpl` to derive each package's display status. */
data class NahwuQuizPackageSummaryRow(
    val id: String,
    val title: String,
    val description: String,
    val order: Int,
    val isActive: Boolean,
    val questionCount: Int,
)

@Dao
interface NahwuQuizPackageDao {
    @Upsert
    suspend fun upsertAll(entities: List<NahwuQuizPackageEntity>)

    @Query("SELECT * FROM nahwu_quiz_packages WHERE id = :id")
    suspend fun getById(id: String): NahwuQuizPackageEntity?

    @Query("SELECT COUNT(*) FROM nahwu_quiz_packages")
    suspend fun count(): Int

    @Query(
        """
        SELECT p.id AS id, p.title AS title, p.description AS description, p.`order` AS `order`,
               p.isActive AS isActive,
               (SELECT COUNT(*) FROM nahwu_quiz_questions q WHERE q.packageId = p.id) AS questionCount
        FROM nahwu_quiz_packages p
        WHERE p.isActive = 1
        ORDER BY p.`order` ASC
        """,
    )
    fun observeSummaries(): Flow<List<NahwuQuizPackageSummaryRow>>
}
