package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sangusantri.app.data.local.entity.NahwuQuizAttemptEntity
import kotlinx.coroutines.flow.Flow

/** The single soonest-started incomplete attempt, joined with its package title — backs the
 * global "Lanjutkan kuis" card on `Nahwu Quiz / Landing`. */
data class NahwuQuizActiveAttemptRow(
    val attemptId: String,
    val packageId: String,
    val packageTitle: String,
    val answeredCount: Int,
    val totalCount: Int,
)

@Dao
interface NahwuQuizAttemptDao {
    @Upsert
    suspend fun upsert(entity: NahwuQuizAttemptEntity)

    @Query("SELECT * FROM nahwu_quiz_attempts WHERE id = :id")
    suspend fun getById(id: String): NahwuQuizAttemptEntity?

    @Query("SELECT * FROM nahwu_quiz_attempts WHERE id = :id")
    fun observeById(id: String): Flow<NahwuQuizAttemptEntity?>

    @Query(
        "SELECT * FROM nahwu_quiz_attempts WHERE packageId = :packageId AND completedAtEpochMillis IS NULL " +
            "ORDER BY startedAtEpochMillis DESC LIMIT 1",
    )
    suspend fun getActiveForPackage(packageId: String): NahwuQuizAttemptEntity?

    /** Every attempt, most-recent first — small bundled-content-sized dataset, grouped by package
     * in `NahwuQuizRepositoryImpl` to derive each package's
     * [com.sangusantri.app.domain.model.NahwuQuizPackageStatus]. */
    @Query("SELECT * FROM nahwu_quiz_attempts ORDER BY startedAtEpochMillis DESC")
    fun observeAll(): Flow<List<NahwuQuizAttemptEntity>>

    @Query(
        """
        SELECT a.id AS attemptId, a.packageId AS packageId, p.title AS packageTitle,
               a.currentQuestionIndex AS answeredCount, a.totalCount AS totalCount
        FROM nahwu_quiz_attempts a
        INNER JOIN nahwu_quiz_packages p ON p.id = a.packageId
        WHERE a.completedAtEpochMillis IS NULL
        ORDER BY a.startedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    fun observeMostRecentActive(): Flow<NahwuQuizActiveAttemptRow?>

    @Query(
        "SELECT * FROM nahwu_quiz_attempts WHERE packageId = :packageId AND completedAtEpochMillis IS NOT NULL " +
            "ORDER BY completedAtEpochMillis DESC",
    )
    fun observeCompletedForPackage(packageId: String): Flow<List<NahwuQuizAttemptEntity>>

    /** One-shot equivalent of [observeCompletedForPackage] — used to find the completed attempt
     * immediately before a given one, for `Hasil Kuis`'s score-delta display. */
    @Query(
        "SELECT * FROM nahwu_quiz_attempts WHERE packageId = :packageId AND completedAtEpochMillis IS NOT NULL " +
            "ORDER BY completedAtEpochMillis DESC",
    )
    suspend fun getCompletedForPackage(packageId: String): List<NahwuQuizAttemptEntity>

    /** Advances resume position and running score atomically at submit time, not on the later
     * "Lanjut" tap — see [NahwuQuizAttemptEntity]'s own doc comment for why. */
    @Query(
        "UPDATE nahwu_quiz_attempts SET currentQuestionIndex = currentQuestionIndex + 1, " +
            "correctCount = correctCount + :increment WHERE id = :attemptId",
    )
    suspend fun advanceAfterAnswer(
        attemptId: String,
        increment: Int,
    )

    @Query("UPDATE nahwu_quiz_attempts SET completedAtEpochMillis = :completedAtEpochMillis WHERE id = :attemptId")
    suspend fun markCompleted(
        attemptId: String,
        completedAtEpochMillis: Long,
    )
}
