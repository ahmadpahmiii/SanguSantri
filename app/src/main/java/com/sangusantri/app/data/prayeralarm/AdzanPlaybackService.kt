package com.sangusantri.app.data.prayeralarm

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.getSystemService
import com.sangusantri.app.MainActivity
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.feature.home.labelRes

/**
 * Plays one announcement — one to three bundled recordings, back to back — and stops.
 *
 * A foreground service because the adzan has to finish whether or not the app is open, and the
 * notification it is obliged to post is also the reader's way to cut it short.
 *
 * Deliberately **not** [com.sangusantri.app.data.audio.QuranMurottalPlayer]: that is a singleton
 * shared by the reader, the mushaf and the hub's "Sedang diputar" block, and an adzan pushed through
 * it would silently take over whatever the reader was in the middle of. This owns a plain
 * [MediaPlayer] instead — three local files played in order needs nothing ExoPlayer offers.
 *
 * Audio goes out on the alarm stream ([AudioAttributes.USAGE_ALARM]) so it is heard on a phone whose
 * ringer is silenced, and takes transient audio focus so music — including this app's own murottal —
 * pauses for it and resumes after.
 */
class AdzanPlaybackService : Service() {
    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val prayer = PrayerName.entries.firstOrNull { it.name == intent?.getStringExtra(EXTRA_PRAYER) }
        val tracks =
            intent?.getStringArrayExtra(EXTRA_TRACKS).orEmpty().mapNotNull { name ->
                AdzanTrack.entries.firstOrNull { it.name == name }
            }
        if (prayer == null || tracks.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification(prayer),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )
        // A second announcement while one is still playing (Imsak's double tarhim can run to within
        // seconds of Subuh) replaces it rather than overlapping it.
        releasePlayer()
        requestFocus()
        play(tracks, index = 0)
        // START_NOT_STICKY: if the system kills this mid-adzan, restarting it later would play the
        // call to prayer at some arbitrary moment afterwards. The next alarm is already armed.
        return START_NOT_STICKY
    }

    private fun play(
        tracks: List<AdzanTrack>,
        index: Int,
    ) {
        val track = tracks.getOrNull(index)
        if (track == null) {
            stopSelf()
            return
        }
        val next =
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setOnCompletionListener {
                    releasePlayer()
                    play(tracks, index + 1)
                }
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "adzan playback failed ($what/$extra)")
                    stopSelf()
                    true
                }
            }
        val opened =
            runCatching {
                resources.openRawResourceFd(track.rawResId()).use { descriptor ->
                    next.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                }
                next.prepare()
            }
        if (opened.isFailure) {
            Log.w(TAG, "adzan track unavailable", opened.exceptionOrNull())
            next.release()
            stopSelf()
            return
        }
        player = next
        next.start()
    }

    /** Transient, not exclusive: whatever was playing is meant to come back afterwards. */
    private fun requestFocus() {
        val manager = getSystemService<AudioManager>() ?: return
        val request =
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                ).build()
        manager.requestAudioFocus(request)
        focusRequest = request
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        focusRequest?.let { getSystemService<AudioManager>()?.abandonAudioFocusRequest(it) }
        focusRequest = null
        super.onDestroy()
    }

    private fun notification(prayer: PrayerName) =
        NotificationCompat
            .Builder(this, PrayerNotificationChannels.ADZAN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.prayer_notification_title, getString(prayer.labelRes())))
            .setContentText(getString(R.string.prayer_notification_body))
            .setContentIntent(openSchedulePendingIntent())
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, getString(R.string.prayer_adzan_stop), stopPendingIntent())
            .build()

    private fun stopPendingIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            REQUEST_STOP,
            Intent(this, AdzanPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openSchedulePendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_OPEN,
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_PRAYER_SCHEDULE, true)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun AdzanTrack.rawResId(): Int =
        when (this) {
            AdzanTrack.TARHIM -> R.raw.tarhim
            AdzanTrack.ADZAN_SUBUH -> R.raw.adzan_subuh
            AdzanTrack.ADZAN -> R.raw.adzan
        }

    companion object {
        private const val TAG = "AdzanPlayback"
        private const val ACTION_STOP = "com.sangusantri.app.action.STOP_ADZAN"
        private const val EXTRA_PRAYER = "prayer"
        private const val EXTRA_TRACKS = "tracks"
        private const val NOTIFICATION_ID = 0x5A1C
        private const val REQUEST_STOP = 0x5A1D
        private const val REQUEST_OPEN = 0x5A1E

        fun intent(
            context: Context,
            prayer: PrayerName,
            tracks: List<AdzanTrack>,
        ): Intent =
            Intent(context, AdzanPlaybackService::class.java).apply {
                putExtra(EXTRA_PRAYER, prayer.name)
                putExtra(EXTRA_TRACKS, tracks.map { it.name }.toTypedArray())
            }
    }
}
