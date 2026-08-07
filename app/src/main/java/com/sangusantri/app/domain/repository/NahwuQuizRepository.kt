package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import kotlinx.coroutines.flow.Flow

/** Nahwu Quiz (`0.0.5`) — bundled static question bank, individual/offline attempts only. */
interface NahwuQuizRepository {
    fun observePackageSummaries(): Flow<List<NahwuQuizPackageSummary>>

    /** The single soonest-started, still-incomplete attempt across every package — `null` when
     * nothing is in progress, hiding the "Lanjutkan kuis" card. */
    fun observeActiveAttempt(): Flow<NahwuQuizActiveAttempt?>

    suspend fun getPackage(packageId: String): NahwuQuizPackage?

    suspend fun getQuestions(packageId: String): List<NahwuQuizQuestion>

    /** Resumes the existing incomplete attempt for [packageId] if one exists, otherwise starts a
     * new one — also how "Ulangi kuis" works, since a completed attempt is never resumed. */
    suspend fun getOrCreateActiveAttempt(packageId: String): NahwuQuizAttempt

    fun observeAttempt(attemptId: String): Flow<NahwuQuizAttempt?>

    /** Atomically advances resume position and running score for one answered question. */
    suspend fun submitAnswer(
        attemptId: String,
        isCorrect: Boolean,
    ): NahwuQuizAttempt

    suspend fun completeAttempt(attemptId: String): NahwuQuizAttempt

    fun observeCompletedAttempts(packageId: String): Flow<List<NahwuQuizAttempt>>

    /** The most recent *previous* completed attempt's score for [packageId], excluding
     * [excludingAttemptId] itself — backs `Hasil Kuis`'s optional score delta. `null` when this is
     * the first completed attempt for the package. */
    suspend fun getPreviousScorePercent(
        packageId: String,
        excludingAttemptId: String,
    ): Int?
}
