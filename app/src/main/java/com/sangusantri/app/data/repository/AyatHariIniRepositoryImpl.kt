package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.sync.ayat.AyatHariIniSyncManager
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.repository.AyatHariIniRepository
import java.time.LocalDate
import javax.inject.Inject

class AyatHariIniRepositoryImpl
@Inject
constructor(
    private val database: SanguSantriDatabase,
    private val syncManager: AyatHariIniSyncManager,
) : AyatHariIniRepository {
    private val scheduleDao get() = database.ayatHariIniDao()
    private val verseDao get() = database.quranVerseDao()
    private val surahDao get() = database.quranSurahDao()

    /**
     * Offline-first, in two senses.
     *
     * **It never waits on the network.** Only Room is read here; whether a sync succeeded this
     * launch is not this method's business.
     *
     * **A day with no entry falls back rather than going blank.** If the device rolls into a date
     * the cached window does not cover and the refresh fails, the most recent published ayat is
     * shown instead of nothing. That is a deliberate trade: the header is a day or two behind
     * rather than empty, and the app is offline-first everywhere else for the same reason.
     *
     * Then the safety net that is not negotiable: the schedule says *which* ayat, and the words are
     * read from the Kemenag tables. A reference the local dataset cannot resolve — an ayat past the
     * end of its surah, or any reference at all before the Quran download has finished — yields
     * `null` and the section is not rendered. The app never shows a reference it cannot back with
     * the official text.
     */
    override suspend fun forDate(date: LocalDate): AyatHariIni? {
        val epochDay = date.toEpochDay()
        val scheduled =
            scheduleDao.getByEpochDay(epochDay) ?: scheduleDao.getLatestOnOrBefore(epochDay)
        val verse = scheduled?.let { verseDao.getByIdentity(it.surahNumber, it.ayatNumber) }
        val surah = verse?.let { surahDao.getByNumber(it.surahNumber) }
        return if (scheduled == null || verse == null || surah == null) {
            null
        } else {
            AyatHariIni(
                surahNumber = verse.surahNumber,
                surahName = surah.latinName,
                ayatNumber = verse.ayatNumber,
                arabicText = verse.arabicText,
                translation = verse.translation,
                theme = scheduled.theme,
            )
        }
    }

    override suspend fun sync(): Result<Unit> = syncManager.syncIfNeeded()
}
