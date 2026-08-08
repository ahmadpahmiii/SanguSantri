package com.sangusantri.app.data.sync.quran

import android.util.Log
import androidx.room.withTransaction
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.data.remote.quran.QuranValidation
import com.sangusantri.app.data.remote.quran.QuranValidator
import com.sangusantri.app.data.remote.quran.api.QuranApiService
import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranEnvelopeDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import com.sangusantri.app.data.sync.isRetryableHttpStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * One complete Quran full-sync attempt (QUR-FR-002/003/004): fetch all 114 surahs and every
 * surah's ayat with bounded concurrency, structurally validate the complete candidate, and — only
 * if every part validates — atomically replace `quran_surahs`/`quran_verses` in one Room
 * transaction. A failure at any point leaves the previous complete dataset (if any) exactly as it
 * was; there is no partial activation and no resumable staging (ADR 0016).
 *
 * The same algorithm serves both "initial preparation" (no local dataset yet) and the seven-day
 * refresh — [QuranSyncMetadata] only decides *when* [sync] is called, not what it does.
 */
class QuranSyncManager
@Inject
constructor(
    private val api: QuranApiService,
    private val database: SanguSantriDatabase,
) {
    private val surahDao get() = database.quranSurahDao()
    private val verseDao get() = database.quranVerseDao()

    /**
     * [onProgress] reports completed-out-of-114 surah fetches as they finish (QUR-FR §6.1's
     * determinate initial-preparation progress) — purely a presentation-layer signal, invoked
     * before the atomic Room commit and unrelated to it; a failure after some progress was
     * reported still leaves Room untouched.
     */
    suspend fun sync(onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> }): QuranSyncResult =
        withContext(Dispatchers.IO) {
            when (val surahs = fetchSurahs()) {
                is FetchOutcome.Failure -> surahs.result
                is FetchOutcome.Success -> fetchAndCommit(surahs.value, onProgress)
            }
        }

    @Suppress("ReturnCount")
    private suspend fun fetchAndCommit(
        surahs: List<QuranSurahDto>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): QuranSyncResult {
        val ayatOutcome = fetchAllAyat(surahs, onProgress)
        if (ayatOutcome is FetchOutcome.Failure) return ayatOutcome.result
        val allAyat = (ayatOutcome as FetchOutcome.Success).value

        val globalValidation = QuranValidator.validateGlobalUniqueness(allAyat)
        if (globalValidation is QuranValidation.Invalid) {
            return QuranSyncResult.PermanentFailure(globalValidation.reason)
        }

        commit(surahs, allAyat)
        return QuranSyncResult.Completed
    }

    /**
     * Wholesale replace inside one transaction — [surahDao.deleteAll] cascades to
     * `quran_verses` via [com.sangusantri.app.data.local.entity.QuranVerseEntity]'s foreign
     * key, so a genuinely shrinking candidate (fewer surahs/ayat than currently stored) can
     * never leave orphaned rows behind, unlike a pure upsert.
     */
    private suspend fun commit(
        surahs: List<QuranSurahDto>,
        ayats: List<QuranAyatDto>,
    ) {
        database.withTransaction {
            surahDao.deleteAll()
            surahDao.insertAll(surahs.map { it.toEntity() })
            verseDao.insertAll(ayats.sortedWith(compareBy({ it.surah }, { it.ayat })).map { it.toEntity() })
        }
    }

    @Suppress("ReturnCount")
    private suspend fun fetchSurahs(): FetchOutcome<List<QuranSurahDto>> {
        val response =
            try {
                api.getSurahs(first = 1, count = QuranValidator.EXPECTED_SURAH_COUNT)
            } catch (io: IOException) {
                Log.w(TAG, "surah list fetch failed", io)
                return FetchOutcome.Failure(QuranSyncResult.RetryableFailure("surah list network error"))
            } catch (malformed: SerializationException) {
                Log.w(TAG, "surah list fetch malformed", malformed)
                return FetchOutcome.Failure(QuranSyncResult.PermanentFailure("malformed surah list body"))
            }
        return toFetchOutcome(response, source = "surah list", validateData = QuranValidator::validateSurahList)
    }

    private suspend fun fetchAllAyat(
        surahs: List<QuranSurahDto>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): FetchOutcome<List<QuranAyatDto>> =
        coroutineScope {
            val semaphore = Semaphore(BOUNDED_CONCURRENCY)
            val completedCount = AtomicInteger(0)
            val results =
                surahs
                    .map { surah ->
                        async {
                            semaphore.withPermit { fetchAyatForSurah(surah) }.also {
                                onProgress(completedCount.incrementAndGet(), surahs.size)
                            }
                        }
                    }.awaitAll()
            val failure = results.filterIsInstance<FetchOutcome.Failure>().firstOrNull()
            if (failure != null) {
                failure
            } else {
                val successes = results.filterIsInstance<FetchOutcome.Success<List<QuranAyatDto>>>()
                FetchOutcome.Success(successes.flatMap { it.value })
            }
        }

    @Suppress("ReturnCount")
    private suspend fun fetchAyatForSurah(surah: QuranSurahDto): FetchOutcome<List<QuranAyatDto>> {
        val response =
            try {
                api.getAyat(surah.id)
            } catch (io: IOException) {
                Log.w(TAG, "ayat fetch failed for surah ${surah.id}", io)
                return FetchOutcome.Failure(
                    QuranSyncResult.RetryableFailure("ayat network error (surah ${surah.id})"),
                )
            } catch (malformed: SerializationException) {
                Log.w(TAG, "ayat fetch malformed for surah ${surah.id}", malformed)
                return FetchOutcome.Failure(
                    QuranSyncResult.PermanentFailure("malformed ayat body (surah ${surah.id})"),
                )
            }
        return toFetchOutcome(response, source = "ayat surah ${surah.id}") { ayats ->
            QuranValidator.validateAyatForSurah(surah, ayats)
        }
    }

    @Suppress("ReturnCount")
    private fun <T> toFetchOutcome(
        response: Response<QuranEnvelopeDto<T>>,
        source: String,
        validateData: (T) -> QuranValidation,
    ): FetchOutcome<T> {
        if (!response.isSuccessful) {
            return FetchOutcome.Failure(classifyHttpFailure(response.code(), source))
        }
        val envelope =
            response.body() ?: return FetchOutcome.Failure(QuranSyncResult.PermanentFailure("empty $source body"))
        val envelopeValidation = QuranValidator.validateEnvelope(envelope.code, envelope.res)
        if (envelopeValidation is QuranValidation.Invalid) {
            return FetchOutcome.Failure(QuranSyncResult.PermanentFailure("$source: ${envelopeValidation.reason}"))
        }
        val dataValidation = validateData(envelope.data)
        if (dataValidation is QuranValidation.Invalid) {
            return FetchOutcome.Failure(QuranSyncResult.PermanentFailure("$source: ${dataValidation.reason}"))
        }
        return FetchOutcome.Success(envelope.data)
    }

    private fun classifyHttpFailure(
        code: Int,
        source: String,
    ): QuranSyncResult =
        if (isRetryableHttpStatus(code)) {
            QuranSyncResult.RetryableFailure("$source HTTP $code")
        } else {
            QuranSyncResult.PermanentFailure("$source HTTP $code")
        }

    private sealed interface FetchOutcome<out T> {
        data class Success<T>(
            val value: T,
        ) : FetchOutcome<T>

        data class Failure(
            val result: QuranSyncResult,
        ) : FetchOutcome<Nothing>
    }

    private companion object {
        const val TAG = "QuranSyncManager"
        const val BOUNDED_CONCURRENCY = 6
    }
}
