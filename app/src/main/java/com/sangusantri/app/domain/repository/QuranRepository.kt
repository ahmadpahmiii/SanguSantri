package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsir
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import kotlinx.coroutines.flow.Flow

/**
 * Standalone Al-Qur'an Kemenag (`0.0.6`, ADR 0016) — a bounded context separate from
 * [ContentRepository], never forced through the amaliyah content-package schema
 * (`docs/engineering/CONTENT_MODEL.md`). Room remains the only UI-readable source of truth; every
 * observe* method reads Room, never a network response directly.
 *
 * One cohesive Quran bounded-context repository — deliberately not split into a second
 * Quran-specific repository per table, which `CODING_STANDARD.md`'s no-duplicate-repository rule
 * warns against (same reasoning as `di/DatabaseModule.kt`'s `TooManyFunctions` suppression below).
 */
@Suppress("TooManyFunctions")
interface QuranRepository {
    fun observeSurahs(): Flow<List<QuranSurah>>

    fun observeVersesBySurah(surahNumber: Int): Flow<List<QuranVerse>>

    /** Every ayat on mushaf pages [fromPage]..[toPage], in printed order across surah boundaries —
     * a halaman is a page of the mushaf, not a slice of one surah. */
    fun observeVersesByPageRange(
        fromPage: Int,
        toPage: Int,
    ): Flow<List<QuranVerse>>

    /** The mushaf page an ayat is printed on, or `null` when the dataset has no such ayat. */
    suspend fun pageOf(
        surahNumber: Int,
        ayatNumber: Int,
    ): Int?

    /** One entry per Juz 1..30 — the first locally ordered verse of each (QUR-FR-007). */
    fun observeJuzStarts(): Flow<List<QuranVerse>>

    fun observeBookmarks(): Flow<List<QuranBookmark>>

    fun observeIsBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Flow<Boolean>

    fun observeReadingState(): Flow<QuranReadingState?>

    fun observeReadingSessions(): Flow<List<QuranReadingSession>>

    suspend fun hasLocalDataset(): Boolean

    /** Runs a full sync only when no local dataset exists yet (QUR-FR-002); a no-op [Ready] when
     * one already does. [onProgress] reports completed-out-of-114 surahs for the determinate
     * initial-preparation UI (QUR-FR §6.1). */
    suspend fun ensureInitialPreparation(
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
    ): QuranPreparationResult

    suspend fun toggleBookmark(
        surahNumber: Int,
        ayatNumber: Int,
    )

    suspend fun setLastRead(
        surahNumber: Int,
        ayatNumber: Int,
        page: Int,
    )

    /** Writes one reading-activity event only when [endAyat] advances beyond [startAyat] — merely
     * opening, closing, or moving backwards must not create an event (QUR-FR-017). */
    suspend fun recordReadingSession(
        surahNumber: Int,
        startAyat: Int,
        endAyat: Int,
    )

    /** Cache-only read, no network call. */
    suspend fun getCachedTafsir(remoteAyatId: Long): QuranTafsir?

    /** Fetches tafsir from Kemenag and caches it on success (QUR-FR-013). */
    suspend fun fetchTafsir(remoteAyatId: Long): QuranTafsirResult
}
