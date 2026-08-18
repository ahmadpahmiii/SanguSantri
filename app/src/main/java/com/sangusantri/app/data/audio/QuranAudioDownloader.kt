package com.sangusantri.app.data.audio

import com.sangusantri.app.di.QuranAudioHttpClient
import com.sangusantri.app.domain.model.QuranAudioDownloadProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches ayah recitation from the CDN onto disk.
 *
 * Both play actions in the addendum are "fetch → store → play", so this is the single path audio
 * arrives by: a tap that needs one ayah and a surah download differ only in how many ayat they ask
 * for and whether progress is reported.
 *
 * Downloads are not resumable. The CDN answers ranged requests with the whole file and sends no
 * `Accept-Ranges`, so a cancelled ayah restarts rather than continuing; at ≤2.3 MB per ayah that is
 * cheaper than the bookkeeping resumption would need.
 */
@Singleton
class QuranAudioDownloader
@Inject
constructor(
    @QuranAudioHttpClient private val httpClient: OkHttpClient,
    private val store: QuranAudioStore,
) {
    /** Downloads in flight, keyed by positional file name — see [downloadAyah]. */
    private val inFlight = ConcurrentHashMap<String, Deferred<Boolean>>()

    /** Downloads run here, not in the caller's scope, so one caller giving up does not abort a fetch
     * another caller is still waiting on. */
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Downloads one ayah unless it is already stored. Returns `false` on any network or write
     * failure — callers surface that as the design's error state rather than retrying silently.
     *
     * **Single-flight.** Three callers can ask for the same ayah at once: the surah-download loop
     * walking 1..N, the player fetching the ayah it is about to play, and the player prefetching the
     * next one. Without deduplication two of them would open writers on the same `.part` path and
     * interleave bytes into a corrupt file, and whichever renamed second could also report a
     * spurious failure for a file that had in fact arrived. Concurrent callers now await one shared
     * download instead.
     */
    suspend fun downloadAyah(
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean {
        if (store.isStored(surahNumber, ayahNumber)) return true
        val key = QuranAudioSource.ayahFileName(surahNumber, ayahNumber)
        val download = inFlight.computeIfAbsent(key) { downloadScope.async { fetchAyah(surahNumber, ayahNumber) } }
        // Registered outside computeIfAbsent: the handler mutates this same map, and running it
        // inside the mapping function would re-enter it. Two-arg remove never evicts a newer entry,
        // and registering twice is harmless.
        download.invokeOnCompletion { inFlight.remove(key, download) }
        return download.await()
    }

    private suspend fun fetchAyah(
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean =
        withContext(Dispatchers.IO) {
            store.ensureDirectory()
            val target = store.file(surahNumber, ayahNumber)
            val partial = store.partialFile(surahNumber, ayahNumber)
            val request = Request.Builder().url(QuranAudioSource.ayahAudioUrl(surahNumber, ayahNumber)).build()
            runCatching {
                httpClient.newCall(request).execute().use { response ->
                    require(response.isSuccessful) { "audio request failed: ${response.code}" }
                    partial.sink().buffer().use { sink -> sink.writeAll(response.body.source()) }
                }
                // Only a fully written file gets the real name, so presence always means playable.
                require(partial.length() > 0) { "empty audio body" }
                require(partial.renameTo(target)) { "could not finalise ${target.name}" }
            }.onFailure {
                partial.delete()
                // A cancelled download must stay cancelled rather than be reported as a failure.
                currentCoroutineContext().ensureActive()
            }.isSuccess
        }

    /**
     * Downloads a whole surah ayah by ayah, emitting progress as each lands. Cancelling collection
     * stops after the ayah in flight; whatever already landed stays on disk and stays playable.
     */
    fun downloadSurah(
        surahNumber: Int,
        ayatCount: Int,
    ): Flow<QuranAudioDownloadProgress> =
        flow {
            var completed = 0
            var bytes = 0L
            emit(progress(surahNumber, completed, ayatCount, bytes))
            for (ayah in 1..ayatCount) {
                currentCoroutineContext().ensureActive()
                if (!downloadAyah(surahNumber, ayah)) continue
                completed++
                bytes += store.file(surahNumber, ayah).length()
                emit(progress(surahNumber, completed, ayatCount, bytes))
            }
            store.refresh()
        }

    private fun progress(
        surahNumber: Int,
        completed: Int,
        total: Int,
        bytes: Long,
    ) = QuranAudioDownloadProgress(
        surahNumber = surahNumber,
        completedAyat = completed,
        totalAyat = total,
        downloadedBytes = bytes,
        estimatedTotalBytes = if (completed == 0) 0L else bytes / completed * total,
    )
}
