package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.QuranBookmarkEntity
import com.sangusantri.app.data.local.entity.QuranReadingSessionEntity
import com.sangusantri.app.data.local.entity.QuranReadingStateEntity
import com.sangusantri.app.data.local.quran.QuranLocalDataset
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.remote.quran.QuranStableVersionConfig
import com.sangusantri.app.data.sync.quran.QuranSyncManager
import com.sangusantri.app.data.sync.quran.QuranSyncResult
import com.sangusantri.app.data.sync.quran.QuranTafsirFetchOutcome
import com.sangusantri.app.data.sync.quran.QuranTafsirManager
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsir
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

// Mirrors QuranRepository's own TooManyFunctions suppression — one cohesive implementation.
@Suppress("TooManyFunctions")
class QuranRepositoryImpl
@Inject
constructor(
    private val database: SanguSantriDatabase,
    private val syncManager: QuranSyncManager,
    private val localDataset: QuranLocalDataset,
    private val stableVersionConfig: QuranStableVersionConfig,
    private val tafsirManager: QuranTafsirManager,
) : QuranRepository {
    private val surahDao get() = database.quranSurahDao()
    private val verseDao get() = database.quranVerseDao()
    private val bookmarkDao get() = database.quranBookmarkDao()
    private val readingStateDao get() = database.quranReadingStateDao()
    private val readingSessionDao get() = database.quranReadingSessionDao()
    private val syncMutex = Mutex()

    override fun observeSurahs(): Flow<List<QuranSurah>> =
        surahDao.observeAll().map { surahs -> surahs.map { it.toDomain() } }

    override fun observeVersesBySurah(surahNumber: Int): Flow<List<QuranVerse>> =
        verseDao.observeBySurah(surahNumber).map { verses -> verses.map { it.toDomain() } }

    override fun observeJuzStarts(): Flow<List<QuranVerse>> =
        verseDao.observeJuzStarts().map { verses -> verses.map { it.toDomain() } }

    override fun observeBookmarks(): Flow<List<QuranBookmark>> =
        bookmarkDao.observeAll().map { bookmarks -> bookmarks.map { it.toDomain() } }

    override fun observeIsBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Flow<Boolean> = bookmarkDao.observeIsBookmarked(surahNumber, ayatNumber)

    override fun observeReadingState(): Flow<QuranReadingState?> = readingStateDao.observe().map { it?.toDomain() }

    override fun observeReadingSessions(): Flow<List<QuranReadingSession>> =
        readingSessionDao.observeAll().map { sessions -> sessions.map { it.toDomain() } }

    override suspend fun hasLocalDataset(): Boolean = localDataset.isComplete()

    override suspend fun ensureInitialPreparation(
        onProgress: (completed: Int, total: Int) -> Unit,
    ): QuranPreparationResult =
        syncMutex.withLock {
            if (hasLocalDataset()) {
                QuranPreparationResult.Ready
            } else {
                runSync(stableVersionConfig.fetchStableVersion(), onProgress)
            }
        }

    private suspend fun runSync(
        stableVersion: Int,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
    ): QuranPreparationResult =
        when (syncManager.sync(stableVersion, onProgress)) {
            is QuranSyncResult.Completed -> QuranPreparationResult.Ready
            is QuranSyncResult.RetryableFailure -> QuranPreparationResult.Failed(retryable = true)
            is QuranSyncResult.PermanentFailure -> QuranPreparationResult.Failed(retryable = false)
        }

    override suspend fun toggleBookmark(
        surahNumber: Int,
        ayatNumber: Int,
    ) {
        if (bookmarkDao.isBookmarked(surahNumber, ayatNumber)) {
            bookmarkDao.delete(surahNumber, ayatNumber)
        } else {
            bookmarkDao.insert(QuranBookmarkEntity(surahNumber, ayatNumber, System.currentTimeMillis()))
        }
    }

    override suspend fun setLastRead(
        surahNumber: Int,
        ayatNumber: Int,
        page: Int,
    ) {
        readingStateDao.upsert(
            QuranReadingStateEntity(
                surahNumber = surahNumber,
                ayatNumber = ayatNumber,
                page = page,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun recordReadingSession(
        surahNumber: Int,
        startAyat: Int,
        endAyat: Int,
    ) {
        if (endAyat <= startAyat) return
        readingSessionDao.insert(
            QuranReadingSessionEntity(
                surahNumber = surahNumber,
                startAyat = startAyat,
                endAyat = endAyat,
                readAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun getCachedTafsir(remoteAyatId: Long): QuranTafsir? =
        tafsirManager.getCached(remoteAyatId)?.toDomain()

    override suspend fun fetchTafsir(remoteAyatId: Long): QuranTafsirResult =
        when (val outcome = tafsirManager.fetchAndCache(remoteAyatId)) {
            is QuranTafsirFetchOutcome.Success -> QuranTafsirResult.Success(outcome.entity.toDomain())
            is QuranTafsirFetchOutcome.Failure -> QuranTafsirResult.Failure(outcome.retryable)
        }
}
