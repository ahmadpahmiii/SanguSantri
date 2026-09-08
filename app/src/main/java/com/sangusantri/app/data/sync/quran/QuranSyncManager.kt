package com.sangusantri.app.data.sync.quran

import android.util.Log
import androidx.room.withTransaction
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.network.safeApiCall
import com.sangusantri.app.core.network.unwrap
import com.sangusantri.app.core.network.validate
import com.sangusantri.app.core.validation.Validation
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.data.remote.quran.QuranValidator
import com.sangusantri.app.data.remote.quran.api.QuranApiService
import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranEnvelopeDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import com.sangusantri.app.data.sync.quran.QuranSyncManager.Companion.BOUNDED_CONCURRENCY
import com.sangusantri.app.data.sync.quran.QuranSyncManager.Companion.MAX_FETCH_ATTEMPTS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * One complete Quran full-sync attempt (QUR-FR-002/003/004): fetch all 114 surahs and every
 * surah's ayat with bounded concurrency, structurally validate the complete candidate, and — only
 * if every part validates — atomically replace `quran_surahs`/`quran_verses` in one Room
 * transaction. A failure at any point leaves the previous complete dataset (if any) exactly as it
 * was; there is no partial activation (ADR 0016, amended).
 *
 * Successfully-fetched surahs are cached in memory ([cachedAyatBySurah]) across attempts within
 * this process, so a retry after a transient per-surah failure (dropped connection, timeout) only
 * re-fetches what actually failed instead of redownloading the whole corpus. This cache is
 * in-memory only — it never survives process death, is discarded the moment the target
 * [stableVersion][sync] changes, and is never read by Room, so it cannot make an incomplete Quran
 * appear complete. This does not reintroduce the durable/persisted resumable staging ADR 0016
 * rejects.
 *
 * The same algorithm serves both initial preparation and an explicitly version-triggered update.
 * Every [sync] call is serialized through [cacheMutex] — including calls from
 * [QuranUpdateWorker], which does not go through [com.sangusantri.app.data.repository.
 * QuranRepositoryImpl]'s own mutex — so the in-memory cache is never read or written
 * concurrently. Any unexpected exception (not just network/serialization errors) is caught and
 * reported as a retryable failure rather than propagating: this class must never crash its caller,
 * whether that caller is an interactive UI coroutine that may be cancelled mid-sync (user leaves
 * the screen/closes the app) or a background [QuranUpdateWorker].
 *
 * One cohesive fetch-validate-commit algorithm decomposed into small private steps (retry,
 * in-memory cache, fetch, validate, commit) — the resulting `TooManyFunctions` suppression below
 * mirrors [com.sangusantri.app.data.repository.QuranRepositoryImpl]'s own, for the same reason.
 */
@Suppress("TooManyFunctions")
class QuranSyncManager @Inject constructor(
    private val api: QuranApiService,
    private val database: SanguSantriDatabase,
) {
    private val surahDao get() = database.quranSurahDao()
    private val verseDao get() = database.quranVerseDao()

    private val cacheMutex = Mutex()
    private var cachedSurahs: List<QuranSurahDto>? = null
    private var cachedForStableVersion: Int? = null
    private val cachedAyatBySurah = ConcurrentHashMap<Int, List<QuranAyatDto>>()

    /**
     * [onProgress] reports completed-out-of-114 surah fetches as they finish (QUR-FR §6.1's
     * determinate initial-preparation progress) — purely a presentation-layer signal, invoked
     * before the atomic Room commit and unrelated to it; a failure after some progress was
     * reported still leaves Room untouched. On a retry that reuses cached surahs, progress starts
     * from the already-cached count rather than zero.
     */
    suspend fun sync(
        stableVersion: Int,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        require(stableVersion > 0)
        cacheMutex.withLock { runCatchingSync(stableVersion, onProgress) }
    }

    // Last line of defence: whatever goes wrong inside a sync attempt (network loss, the caller
    // coroutine being cancelled because the user closed the app or navigated away, or a genuinely
    // unexpected bug) must resolve to an ApiResult, never an uncaught exception — the caller
    // may be running on viewModelScope, which crashes the process on an uncaught exception.
    // Cancellation is rethrown, never swallowed, so structured concurrency still works correctly.
    @Suppress("TooGenericExceptionCaught")
    private suspend fun runCatchingSync(
        stableVersion: Int,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ApiResult<Unit> = try {
        performSync(stableVersion, onProgress)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (unexpected: Exception) {
        Log.w(TAG, "unexpected sync failure: ${unexpected::class.java.simpleName}")
        FirebaseCrashlytics.getInstance().recordException(unexpected)
        // Deliberately not cleared: whatever was cached is still valid, validated data: an
        // unexpected failure here (e.g. mid-commit) can only be helped, never hurt, by keeping it
        // for the next attempt.
        ApiResult.NetworkError("unexpected sync error")
    }

    @Suppress("ReturnCount")
    private suspend fun performSync(
        stableVersion: Int,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ApiResult<Unit> {
        if (cachedForStableVersion != stableVersion) {
            cachedSurahs = null
            cachedAyatBySurah.clear()
            cachedForStableVersion = stableVersion
        }
        val surahs =
            cachedSurahs ?: when (val outcome = fetchSurahs()) {
                is ApiResult.Failure -> return outcome
                is ApiResult.Success -> outcome.data.also { cachedSurahs = it }
            }
        val result = fetchAndCommit(surahs, stableVersion, onProgress)
        if (result is ApiResult.Success || (result is ApiResult.Failure && !result.isRetryable)) {
            // Completed: the cache is now stale, Room has it. PermanentFailure: the cached data
            // itself may be implicated (e.g. cross-surah duplicate ayat) — safer to discard and
            // start clean than to keep retrying against data that provoked a permanent failure.
            clearCache()
        }
        return result
    }

    private fun clearCache() {
        cachedSurahs = null
        cachedForStableVersion = null
        cachedAyatBySurah.clear()
    }

    @Suppress("ReturnCount")
    private suspend fun fetchAndCommit(
        surahs: List<QuranSurahDto>,
        stableVersion: Int,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ApiResult<Unit> {
        val ayatOutcome = fetchPendingAyat(surahs, onProgress)
        if (ayatOutcome is ApiResult.Failure) return ayatOutcome
        val allAyat = surahs.flatMap { cachedAyatBySurah.getValue(it.id) }

        val globalValidation = QuranValidator.validateGlobalUniqueness(allAyat)
        if (globalValidation is Validation.Invalid) {
            return ApiResult.MalformedResponse(globalValidation.reason)
        }

        commit(surahs, allAyat, stableVersion)
        return ApiResult.Success(Unit)
    }

    /**
     * Wholesale replace inside one transaction — [surahDao.deleteAll] cascades to
     * `quran_verses` via [com.sangusantri.app.data.local.entity.QuranVerseEntity]'s foreign
     * key, so a genuinely shrinking candidate (fewer surahs/ayat than currently stored) can
     * never leave orphaned rows behind, unlike a pure upsert. The same transaction clears cached
     * tafsir whose remote ids may no longer identify the same source rows and advances the applied
     * stable version, so a crash can never expose a new corpus with an old version marker.
     */
    private suspend fun commit(
        surahs: List<QuranSurahDto>,
        ayats: List<QuranAyatDto>,
        stableVersion: Int,
    ) {
        database.withTransaction {
            surahDao.deleteAll()
            surahDao.insertAll(surahs.map { it.toEntity() })
            verseDao.insertAll(ayats.sortedWith(compareBy({ it.surah }, { it.ayat })).map { it.toEntity() })
            database.quranTafsirDao().deleteAll()
            database.appMetadataDao().upsert(
                AppMetadataEntity(
                    key = QuranSyncMetadata.KEY_APPLIED_STABLE_VERSION,
                    value = stableVersion.toString(),
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    private suspend fun fetchSurahs(): ApiResult<List<QuranSurahDto>> = fetchWithRetry(
        source = "surah list",
        validateData = QuranValidator::validateSurahList,
    ) { api.getSurahs(first = 1, count = QuranValidator.EXPECTED_SURAH_COUNT) }

    /** Only surahs not already in [cachedAyatBySurah] are fetched — the point of the whole cache.
     * A concurrent [ConcurrentHashMap] write per surah is required here: up to [BOUNDED_CONCURRENCY]
     * fetches complete around the same time on different IO threads. */
    private suspend fun fetchPendingAyat(
        surahs: List<QuranSurahDto>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ApiResult<Unit> = coroutineScope {
        val pending = surahs.filterNot { cachedAyatBySurah.containsKey(it.id) }
        val completedCount = AtomicInteger(cachedAyatBySurah.size)
        onProgress(completedCount.get(), surahs.size)
        if (pending.isEmpty()) {
            return@coroutineScope ApiResult.Success(Unit)
        }

        val semaphore = Semaphore(BOUNDED_CONCURRENCY)
        val results =
            pending
                .map { surah ->
                    async {
                        val outcome = semaphore.withPermit { fetchAyatForSurah(surah) }
                        if (outcome is ApiResult.Success) {
                            cachedAyatBySurah[surah.id] = outcome.data
                        }
                        onProgress(completedCount.incrementAndGet(), surahs.size)
                        outcome
                    }
                }.awaitAll()
        results.filterIsInstance<ApiResult.Failure>().firstOrNull() ?: ApiResult.Success(Unit)
    }

    private suspend fun fetchAyatForSurah(surah: QuranSurahDto): ApiResult<List<QuranAyatDto>> = fetchWithRetry(
        source = "ayat surah ${surah.id}",
        validateData = { ayats -> QuranValidator.validateAyatForSurah(surah, ayats) },
    ) { api.getAyat(surah.id) }

    /**
     * Transparently retries a single request up to [MAX_FETCH_ATTEMPTS] times when the failure is
     * classified retryable (dropped/incomplete connection, timeout, or a retryable HTTP status) —
     * this absorbs the common one-off transient blip (e.g. `unexpected end of stream` from a stale
     * pooled connection) without ever surfacing a failure to the caller. A permanent failure
     * (malformed body, non-retryable HTTP status) is never retried.
     */
    private suspend fun <T : Any> fetchWithRetry(
        source: String,
        validateData: (T) -> Validation,
        call: suspend () -> Response<QuranEnvelopeDto<T>>,
    ): ApiResult<T> {
        var attempt = 1
        while (true) {
            val outcome = attemptSingleFetch(source, validateData, call)
            val retryable = outcome is ApiResult.Failure && outcome.isRetryable
            if (outcome is ApiResult.Success || !retryable || attempt >= MAX_FETCH_ATTEMPTS) {
                return outcome
            }
            Log.w(TAG, "$source attempt $attempt failed, retrying")
            delay(RETRY_BACKOFF_BASE_MILLIS * attempt)
            attempt++
        }
    }

    /**
     * One request, its envelope unwrapped and its payload structurally validated.
     *
     * Everything that used to live here — the `IOException`/`SerializationException` catches, the
     * `isSuccessful` check, the null-body check, the envelope check, the HTTP retryability
     * classification — is now [safeApiCall] and its two composers, shared with every other
     * endpoint in the app.
     */
    private suspend fun <T : Any> attemptSingleFetch(
        source: String,
        validateData: (T) -> Validation,
        call: suspend () -> Response<QuranEnvelopeDto<T>>,
    ): ApiResult<T> = safeApiCall(source, call).unwrap(source).validate(source, validateData)

    private companion object {
        const val TAG = "QuranSyncManager"
        const val BOUNDED_CONCURRENCY = 6
        const val MAX_FETCH_ATTEMPTS = 3
        const val RETRY_BACKOFF_BASE_MILLIS = 300L
    }
}
