package com.sangusantri.app.data.local.nahwuquiz

import android.content.Context
import androidx.room.withTransaction
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizBankDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed interface NahwuQuizBootstrapOutcome {
    data object AlreadySeeded : NahwuQuizBootstrapOutcome

    data class Imported(
        val packageCount: Int,
    ) : NahwuQuizBootstrapOutcome

    data class Rejected(
        val reason: String,
    ) : NahwuQuizBootstrapOutcome
}

/**
 * Reads the bundled question bank from [android.content.res.AssetManager] and imports it once —
 * unlike `BundledContentBootstrapper`, there is only one source (no remote sync in this milestone's
 * scope) and no per-package version to reconcile, so import is gated purely on "have any packages
 * ever been seeded", not a version comparison. Safe to call on every launch: once
 * [com.sangusantri.app.data.local.dao.NahwuQuizPackageDao.count] is non-zero, every later call is a
 * cheap no-op. [bootstrapIfNeeded] is also re-callable directly as a "Coba lagi" retry action if the
 * very first attempt failed (e.g. a malformed bundled asset), since a failed attempt leaves the
 * package table empty and therefore still eligible to import.
 */
class NahwuQuizBootstrapper
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val database: SanguSantriDatabase,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val packageDao get() = database.nahwuQuizPackageDao()
    private val questionDao get() = database.nahwuQuizQuestionDao()

    suspend fun bootstrapIfNeeded(): NahwuQuizBootstrapOutcome =
        withContext(Dispatchers.IO) {
            if (packageDao.count() > 0) return@withContext NahwuQuizBootstrapOutcome.AlreadySeeded

            val bank =
                readBank() ?: return@withContext NahwuQuizBootstrapOutcome.Rejected(
                    "unable to read or parse bundled $BANK_FILE_NAME",
                )
            val validation = NahwuQuizValidator.validate(bank)
            if (validation is NahwuQuizValidation.Invalid) {
                return@withContext NahwuQuizBootstrapOutcome.Rejected(validation.reason)
            }

            writeBank(bank)
            NahwuQuizBootstrapOutcome.Imported(bank.packages.size)
        }

    private suspend fun writeBank(bank: NahwuQuizBankDto) {
        database.withTransaction {
            packageDao.upsertAll(bank.packages.map { it.toEntity() })
            bank.packages.forEach { pkg ->
                questionDao.upsertAll(
                    pkg.questions.mapIndexed { index, question ->
                        question.toEntity(packageId = pkg.id, position = index + 1)
                    },
                )
            }
        }
    }

    private fun readBank(): NahwuQuizBankDto? =
        runCatching {
            json.decodeFromString<NahwuQuizBankDto>(
                context.assets
                    .open("$QUIZ_ASSET_DIR/$BANK_FILE_NAME")
                    .use { it.readBytes() }
                    .decodeToString(),
            )
        }.getOrNull()

    private companion object {
        const val QUIZ_ASSET_DIR = "nahwu_quiz"
        const val BANK_FILE_NAME = "nahwu_quiz_bank.json"
    }
}
