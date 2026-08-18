package com.sangusantri.app.data.audio

import android.content.Context
import com.sangusantri.app.domain.model.QuranAudioLibrary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The on-device murottal library: where ayah files live, which ones are present, and how much space
 * they take.
 *
 * Stored under `filesDir`, not `cacheDir` — the download block promises "dengar tanpa jaringan", and
 * the OS is free to evict a cache directory at any time, which would quietly break that promise.
 *
 * A partially written file would be indistinguishable from a complete one, so [QuranAudioDownloader]
 * writes to a `.part` sibling and renames on success; only fully written files ever carry the real
 * name, which is what makes presence a reliable "playable offline" signal.
 */
@Singleton
class QuranAudioStore
@Inject
constructor(
    @ApplicationContext context: Context,
) {
    private val directory = File(context.filesDir, AUDIO_DIRECTORY_NAME)

    private val _library = MutableStateFlow(QuranAudioLibrary())
    val library: StateFlow<QuranAudioLibrary> = _library.asStateFlow()

    fun file(
        surahNumber: Int,
        ayahNumber: Int,
    ): File = File(directory, QuranAudioSource.ayahFileName(surahNumber, ayahNumber))

    fun partialFile(
        surahNumber: Int,
        ayahNumber: Int,
    ): File = File(directory, "${QuranAudioSource.ayahFileName(surahNumber, ayahNumber)}$PARTIAL_SUFFIX")

    fun isStored(
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean = file(surahNumber, ayahNumber).length() > 0

    fun ensureDirectory() {
        if (!directory.exists()) directory.mkdirs()
    }

    /** Rescans the directory. Called once at startup and after every download or deletion, rather
     * than on each UI read — 6236 possible files make a per-recomposition `exists()` sweep wasteful. */
    suspend fun refresh() {
        val scanned =
            withContext(Dispatchers.IO) {
                val ayahCounts = mutableMapOf<Int, Int>()
                val byteCounts = mutableMapOf<Int, Long>()
                directory.listFiles()?.forEach { file ->
                    val position = QuranAudioSource.parseFileName(file.name) ?: return@forEach
                    val length = file.length()
                    if (length <= 0) return@forEach
                    ayahCounts[position.first] = (ayahCounts[position.first] ?: 0) + 1
                    byteCounts[position.first] = (byteCounts[position.first] ?: 0L) + length
                }
                QuranAudioLibrary(ayahCountBySurah = ayahCounts, bytesBySurah = byteCounts)
            }
        _library.value = scanned
    }

    suspend fun deleteSurah(surahNumber: Int) {
        withContext(Dispatchers.IO) {
            directory.listFiles()?.forEach { file ->
                if (QuranAudioSource.parseFileName(file.name)?.first == surahNumber) file.delete()
            }
        }
        refresh()
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            directory.listFiles()?.forEach(File::delete)
        }
        refresh()
    }

    private companion object {
        const val AUDIO_DIRECTORY_NAME = "murottal"
        const val PARTIAL_SUFFIX = ".part"
    }
}
