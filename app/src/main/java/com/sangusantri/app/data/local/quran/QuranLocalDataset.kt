package com.sangusantri.app.data.local.quran

import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.remote.quran.QuranValidator
import javax.inject.Inject

/** Canonical completeness check shared by the entry gate and version-update scheduler. */
class QuranLocalDataset
@Inject
constructor(
    private val database: SanguSantriDatabase,
) {
    suspend fun isComplete(): Boolean {
        val surahDao = database.quranSurahDao()
        if (surahDao.count() != QuranValidator.EXPECTED_SURAH_COUNT) return false
        val expectedAyatCount = surahDao.totalExpectedAyatCount()
        return expectedAyatCount > 0 && database.quranVerseDao().count() == expectedAyatCount
    }
}
