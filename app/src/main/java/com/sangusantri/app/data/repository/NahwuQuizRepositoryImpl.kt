package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.NahwuQuizAttemptDao
import com.sangusantri.app.data.local.dao.NahwuQuizPackageDao
import com.sangusantri.app.data.local.dao.NahwuQuizQuestionDao
import com.sangusantri.app.data.local.entity.NahwuQuizAttemptEntity
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageStatus
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// The function count follows directly from NahwuQuizRepository's own interface surface — splitting
// this into two repositories for one over-threshold function would be an artificial boundary with
// no real domain reason, which CODING_STANDARD.md also warns against.
@Suppress("TooManyFunctions")
class NahwuQuizRepositoryImpl
@Inject
constructor(
    private val packageDao: NahwuQuizPackageDao,
    private val questionDao: NahwuQuizQuestionDao,
    private val attemptDao: NahwuQuizAttemptDao,
) : NahwuQuizRepository {
    override fun observePackageSummaries(): Flow<List<NahwuQuizPackageSummary>> =
        combine(packageDao.observeSummaries(), attemptDao.observeAll()) { packages, attempts ->
            val mostRecentAttemptByPackage = attempts.groupBy { it.packageId }.mapValues { it.value.first() }
            packages.map { row ->
                val quizPackage = row.toDomain()
                val latestAttempt = mostRecentAttemptByPackage[row.id]
                NahwuQuizPackageSummary(
                    quizPackage = quizPackage,
                    status = statusFor(quizPackage, latestAttempt),
                    answeredCount = answeredCountFor(latestAttempt),
                )
            }
        }

    private fun answeredCountFor(attempt: NahwuQuizAttemptEntity?): Int? =
        when {
            attempt == null -> null
            attempt.completedAtEpochMillis != null -> attempt.totalCount
            else -> attempt.currentQuestionIndex
        }

    override fun observeActiveAttempt(): Flow<NahwuQuizActiveAttempt?> =
        attemptDao.observeMostRecentActive().map { row ->
            row?.let {
                NahwuQuizActiveAttempt(
                    attemptId = it.attemptId,
                    packageId = it.packageId,
                    packageTitle = it.packageTitle,
                    answeredCount = it.answeredCount,
                    totalCount = it.totalCount,
                )
            }
        }

    override suspend fun getPackage(packageId: String): NahwuQuizPackage? {
        val entity = packageDao.getById(packageId) ?: return null
        val questionCount = questionDao.getByPackageId(packageId).size
        return entity.toDomain(questionCount)
    }

    override suspend fun getQuestions(packageId: String): List<NahwuQuizQuestion> =
        questionDao.getByPackageId(packageId).map { it.toDomain() }

    override suspend fun getOrCreateActiveAttempt(packageId: String): NahwuQuizAttempt {
        attemptDao.getActiveForPackage(packageId)?.let { return it.toDomain() }
        val totalCount = questionDao.getByPackageId(packageId).size
        val created =
            NahwuQuizAttemptEntity(
                id = UUID.randomUUID().toString(),
                packageId = packageId,
                startedAtEpochMillis = System.currentTimeMillis(),
                completedAtEpochMillis = null,
                currentQuestionIndex = 0,
                correctCount = 0,
                totalCount = totalCount,
            )
        attemptDao.upsert(created)
        return created.toDomain()
    }

    override fun observeAttempt(attemptId: String): Flow<NahwuQuizAttempt?> =
        attemptDao.observeById(attemptId).map { it?.toDomain() }

    override suspend fun submitAnswer(
        attemptId: String,
        isCorrect: Boolean,
    ): NahwuQuizAttempt {
        attemptDao.advanceAfterAnswer(attemptId, if (isCorrect) 1 else 0)
        return requireNotNull(attemptDao.getById(attemptId)?.toDomain()) {
            "attempt $attemptId disappeared during submitAnswer"
        }
    }

    override suspend fun completeAttempt(attemptId: String): NahwuQuizAttempt {
        attemptDao.markCompleted(attemptId, System.currentTimeMillis())
        return requireNotNull(attemptDao.getById(attemptId)?.toDomain()) {
            "attempt $attemptId disappeared during completeAttempt"
        }
    }

    override fun observeCompletedAttempts(packageId: String): Flow<List<NahwuQuizAttempt>> =
        attemptDao.observeCompletedForPackage(packageId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPreviousScorePercent(
        packageId: String,
        excludingAttemptId: String,
    ): Int? =
        attemptDao
            .getCompletedForPackage(packageId)
            .firstOrNull { it.id != excludingAttemptId }
            ?.toDomain()
            ?.scorePercent

    private fun statusFor(
        quizPackage: NahwuQuizPackage,
        latestAttempt: NahwuQuizAttemptEntity?,
    ): NahwuQuizPackageStatus =
        when {
            quizPackage.questionCount == 0 -> NahwuQuizPackageStatus.UNAVAILABLE
            latestAttempt == null -> NahwuQuizPackageStatus.NEW
            latestAttempt.completedAtEpochMillis != null -> NahwuQuizPackageStatus.COMPLETED
            else -> NahwuQuizPackageStatus.IN_PROGRESS
        }
}
