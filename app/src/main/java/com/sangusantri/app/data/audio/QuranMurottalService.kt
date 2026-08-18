package com.sangusantri.app.data.audio

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Hosts the murottal session so recitation continues after the reader is closed, with the usual
 * system media notification and lock-screen controls.
 *
 * It deliberately does **not** own an [androidx.media3.exoplayer.ExoPlayer] of its own — it publishes
 * [QuranMurottalPlayer]'s singleton instance. Two players would mean the hub's "Sedang diputar" block
 * and the notification could disagree about what is playing. For the same reason the player is not
 * released here: the app owns its lifetime, and the service can come and go beneath it.
 */
@AndroidEntryPoint
class QuranMurottalService : MediaSessionService() {
    @Inject
    lateinit var murottalPlayer: QuranMurottalPlayer

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, murottalPlayer.exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
