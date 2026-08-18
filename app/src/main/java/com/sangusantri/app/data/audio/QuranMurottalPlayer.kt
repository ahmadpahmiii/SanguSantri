package com.sangusantri.app.data.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.sangusantri.app.domain.model.QuranMurottalSpeed
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranMurottalStatus
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one murottal brain: what is playing, what plays next, and how each ayah gets onto disk first.
 *
 * One [MediaItem] is loaded at a time rather than a full-surah playlist. Every behaviour the
 * addendum asks for is per-ayah — "Ulangi 3×", "Putar ayat ini saja", a progress line *inside* the
 * current ayah, and stopping exactly at a surah boundary unless the cross-surah switch is on — and
 * all of those are boundary logic that an ExoPlayer playlist would hide rather than help with.
 *
 * A [Singleton] because the reader, the mushaf page, the hub's "Sedang diputar" block and
 * [QuranMurottalService] must all see one playback, not one per screen. The service hosts *this*
 * player instance, which is what lets recitation continue after the reader is left.
 */
// Transport controls (play/pause/next/previous/stop/speed) plus the queue-advance helpers are the
// natural surface of a player; splitting them across classes would only move the same calls behind
// an extra hop, the way `QuranRepository` documents for its own cohesive bounded context.
@Suppress("TooManyFunctions")
@Singleton
class QuranMurottalPlayer
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val store: QuranAudioStore,
    private val downloader: QuranAudioDownloader,
    private val quranRepository: QuranRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(QuranMurottalState())
    val state: StateFlow<QuranMurottalState> = _state.asStateFlow()

    /** Set from the murottal panel's "Lanjut otomatis antarsurah" switch. */
    var continueAcrossSurah: Boolean = true

    private var prepareJob: Job? = null
    private var tickJob: Job? = null
    private var prefetchJob: Job? = null

    /**
     * A controller connected to [QuranMurottalService]'s session, held only so the session counts as
     * in use.
     *
     * Media3 posts the media notification and promotes the service to the foreground from the code
     * path a controller connection triggers; starting the service alone leaves the notification
     * unposted, so playback would keep going with no way to pause it from outside the app and no
     * foreground status protecting it from being reclaimed. The UI still drives [exoPlayer] directly —
     * the session wraps that same instance, so there is only ever one playback state.
     */
    private var notificationController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    /** Ayat counts per surah, read once from Room — needed to know where a surah ends. */
    private var ayatCounts: Map<Int, Int> = emptyMap()
    private var surahNames: Map<Int, String> = emptyMap()

    internal val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) onAyahFinished()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        val current = _state.value
                        if (current.status == QuranMurottalStatus.PREPARING) return
                        if (!current.isActive) return
                        _state.value =
                            current.copy(
                                status = if (isPlaying) QuranMurottalStatus.PLAYING else QuranMurottalStatus.PAUSED,
                            )
                        if (isPlaying) startTicking()
                    }
                },
            )
        }
    }

    /**
     * Plays [ayahNumber] and, unless [singleAyahOnly], continues through the surah afterwards.
     *
     * [repeatCount] above 1 replays this same ayah that many times before moving on ("Ulangi 3×").
     */
    fun play(
        surahNumber: Int,
        ayahNumber: Int,
        singleAyahOnly: Boolean = false,
        repeatCount: Int = 1,
    ) {
        prepareJob?.cancel()
        prepareJob =
            scope.launch {
                ensureCatalog()
                val alreadyStored = store.isStored(surahNumber, ayahNumber)
                _state.value =
                    _state.value.copy(
                        status = QuranMurottalStatus.PREPARING,
                        surahNumber = surahNumber,
                        surahName = surahNames[surahNumber].orEmpty(),
                        ayahNumber = ayahNumber,
                        nextAyahNumber = nextAyahOf(surahNumber, ayahNumber, singleAyahOnly),
                        positionFraction = 0f,
                        isDownloading = !alreadyStored,
                        repeatRemaining = repeatCount.coerceAtLeast(1),
                        singleAyahOnly = singleAyahOnly,
                        queuedSurahNames = queuedNamesFrom(surahNumber, singleAyahOnly),
                    )

                val downloaded = downloader.downloadAyah(surahNumber, ayahNumber)
                if (!downloaded) {
                    _state.value = _state.value.copy(status = QuranMurottalStatus.ERROR, isDownloading = false)
                    return@launch
                }
                if (!alreadyStored) store.refresh()

                startService()
                exoPlayer.setMediaItem(mediaItemFor(surahNumber, ayahNumber))
                exoPlayer.setPlaybackSpeed(_state.value.speed.multiplier)
                exoPlayer.prepare()
                exoPlayer.play()
                _state.value = _state.value.copy(status = QuranMurottalStatus.PLAYING, isDownloading = false)
                startTicking()
                prefetchNext(surahNumber, ayahNumber, singleAyahOnly)
            }
    }

    fun togglePlayPause() {
        if (!_state.value.isActive) return
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun skipToNext() {
        val current = _state.value
        val surah = current.surahNumber ?: return
        val ayah = current.ayahNumber ?: return
        // Skipping is an explicit request to keep going, so it ignores a single-ayah restriction.
        val next = nextAyahOf(surah, ayah, singleAyahOnly = false)
        if (next != null) {
            play(surah, next)
        } else {
            advanceToNextSurah(surah)
        }
    }

    fun skipToPrevious() {
        val current = _state.value
        val surah = current.surahNumber ?: return
        val ayah = current.ayahNumber ?: return
        if (ayah > 1) play(surah, ayah - 1) else stop()
    }

    fun setSpeed(speed: QuranMurottalSpeed) {
        _state.value = _state.value.copy(speed = speed)
        if (_state.value.isActive) exoPlayer.setPlaybackSpeed(speed.multiplier)
    }

    /** Cancels a preparing ayah ("Batal" in the loading state) or stops playback ("close"). */
    fun stop() {
        prepareJob?.cancel()
        prefetchJob?.cancel()
        tickJob?.cancel()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _state.value = QuranMurottalState(speed = _state.value.speed)
        releaseNotificationController()
        context.stopService(Intent(context, QuranMurottalService::class.java))
    }

    /** The stored file plus the metadata the system media notification and lock screen display —
     * without it the notification would show a blank title while reciting. */
    private fun mediaItemFor(
        surahNumber: Int,
        ayahNumber: Int,
    ): MediaItem =
        MediaItem
            .Builder()
            .setUri(store.file(surahNumber, ayahNumber).toURI().toString())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle("${surahNames[surahNumber].orEmpty()} : $ayahNumber")
                    .setArtist(QuranAudioSource.RECITER_NAME)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build(),
            ).build()

    private fun onAyahFinished() {
        val current = _state.value
        val surah = current.surahNumber ?: return
        val ayah = current.ayahNumber ?: return

        when {
            // "Ulangi 3×" replays this same ayah before the queue moves on.
            current.repeatRemaining > 1 ->
                play(surah, ayah, current.singleAyahOnly, current.repeatRemaining - 1)

            current.singleAyahOnly -> stop()

            else -> {
                val next = nextAyahOf(surah, ayah, singleAyahOnly = false)
                if (next != null) play(surah, next) else advanceToNextSurah(surah)
            }
        }
    }

    private fun advanceToNextSurah(surahNumber: Int) {
        if (!continueAcrossSurah || surahNumber >= TOTAL_SURAHS) {
            stop()
            return
        }
        play(surahNumber + 1, 1)
    }

    /** `null` when this ayah is the last of the surah, or when only this ayah should play. */
    private fun nextAyahOf(
        surahNumber: Int,
        ayahNumber: Int,
        singleAyahOnly: Boolean,
    ): Int? =
        ayatCounts[surahNumber]
            ?.takeUnless { singleAyahOnly }
            ?.let { count -> (ayahNumber + 1).takeIf { it <= count } }

    /** Names the surahs the queue will reach after this one, for the panel's "Antrean:" line. */
    private fun queuedNamesFrom(
        surahNumber: Int,
        singleAyahOnly: Boolean,
    ): List<String> {
        if (singleAyahOnly || !continueAcrossSurah) return emptyList()
        return (surahNumber + 1..minOf(surahNumber + QUEUE_PREVIEW_LENGTH, TOTAL_SURAHS))
            .mapNotNull { surahNames[it] }
    }

    /** Warms the next ayah while this one plays so the queue does not stall at each boundary. */
    private fun prefetchNext(
        surahNumber: Int,
        ayahNumber: Int,
        singleAyahOnly: Boolean,
    ) {
        val next = nextAyahOf(surahNumber, ayahNumber, singleAyahOnly) ?: return
        prefetchJob?.cancel()
        prefetchJob = scope.launch { downloader.downloadAyah(surahNumber, next) }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob =
            scope.launch {
                while (exoPlayer.isPlaying) {
                    val duration = exoPlayer.duration
                    val fraction =
                        if (duration > 0) {
                            (exoPlayer.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    _state.value = _state.value.copy(positionFraction = fraction)
                    delay(POSITION_TICK_MILLIS)
                }
            }
    }

    private suspend fun ensureCatalog() {
        if (ayatCounts.isNotEmpty()) return
        val surahs = quranRepository.observeSurahs().first()
        ayatCounts = surahs.associate { it.number to it.ayatCount }
        surahNames = surahs.associate { it.number to it.latinName }
    }

    /** Media3 only promotes itself to a foreground service once the service has been started; the
     * caller is always a user tap in the foreground, so this is never a background start. */
    private fun startService() {
        context.startService(Intent(context, QuranMurottalService::class.java))
        connectNotificationController()
    }

    /** Connects [notificationController] once per playback session — see its own comment for why the
     * notification depends on it. Safe to call repeatedly; it no-ops while one is already connected. */
    private fun connectNotificationController() {
        if (notificationController != null || controllerFuture != null) return
        val token = SessionToken(context, ComponentName(context, QuranMurottalService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener({
            notificationController = runCatching { future.get() }.getOrNull()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun releaseNotificationController() {
        notificationController?.release()
        notificationController = null
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
    }

    private companion object {
        const val TOTAL_SURAHS = 114
        const val QUEUE_PREVIEW_LENGTH = 2
        const val POSITION_TICK_MILLIS = 200L
    }
}
