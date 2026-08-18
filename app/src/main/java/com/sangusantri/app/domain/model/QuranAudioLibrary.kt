package com.sangusantri.app.domain.model

/**
 * What murottal audio is stored on this device.
 *
 * Derived from the audio directory itself rather than a Room index: the file names already encode
 * surah and ayah, so a second table would only restate what a directory listing says — and, because
 * the project's standing policy is `fallbackToDestructiveMigration(dropAllTables = true)`, adding a
 * table would mean the next schema bump silently deletes every downloaded recitation along with the
 * Quran corpus. Files outlive that.
 */
data class QuranAudioLibrary(
    /** Downloaded ayah count per surah number. Absent key means nothing stored for that surah. */
    val ayahCountBySurah: Map<Int, Int> = emptyMap(),
    val bytesBySurah: Map<Int, Long> = emptyMap(),
) {
    val surahCount: Int get() = ayahCountBySurah.count { it.value > 0 }

    val totalBytes: Long get() = bytesBySurah.values.sum()

    fun storedAyahCount(surahNumber: Int): Int = ayahCountBySurah[surahNumber] ?: 0

    fun bytes(surahNumber: Int): Long = bytesBySurah[surahNumber] ?: 0L

    /** A surah counts as fully stored only when every one of its ayat has a file — a partially
     * downloaded surah must never advertise itself as offline-ready. */
    fun isSurahComplete(
        surahNumber: Int,
        ayatCount: Int,
    ): Boolean = ayatCount > 0 && storedAyahCount(surahNumber) >= ayatCount
}

/** Per-surah download progress while a download is actually running (design frame `4c`/`4d`). */
data class QuranAudioDownloadProgress(
    val surahNumber: Int,
    val completedAyat: Int,
    val totalAyat: Int,
    val downloadedBytes: Long,
    /** Projected total, extrapolated from the average size of what has arrived so far — the CDN
     * sends no `Content-Length` for the surah as a whole and honours no range requests, so an exact
     * figure is not knowable before the last ayah lands. */
    val estimatedTotalBytes: Long,
) {
    val fraction: Float get() = if (totalAyat <= 0) 0f else completedAyat.toFloat() / totalAyat
}
