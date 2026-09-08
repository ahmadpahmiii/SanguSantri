package com.sangusantri.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.sangusantri.app.data.local.nahwuquiz.NahwuQuizBootstrapOutcome
import com.sangusantri.app.data.local.nahwuquiz.NahwuQuizBootstrapper
import com.sangusantri.app.data.prayeralarm.PrayerAlarmScheduler
import com.sangusantri.app.data.prayeralarm.PrayerNotificationChannels
import com.sangusantri.app.data.reminder.ReminderNotificationChannel
import com.sangusantri.app.data.sync.ContentSyncScheduler
import com.sangusantri.app.data.sync.quran.QuranUpdateScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SanguSantriApplication :
    Application(),
    Configuration.Provider,
    SingletonImageLoader.Factory {
    @Inject
    lateinit var nahwuQuizBootstrapper: NahwuQuizBootstrapper

    @Inject
    lateinit var contentSyncScheduler: ContentSyncScheduler

    @Inject
    lateinit var quranUpdateScheduler: QuranUpdateScheduler

    @Inject
    lateinit var prayerAlarmScheduler: PrayerAlarmScheduler

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Cheap and synchronous (creating an already-existing channel is a no-op) — safe to call
        // on every launch, unlike the network-touching work below.
        ReminderNotificationChannel.ensureCreated(this)
        PrayerNotificationChannels.ensureCreated(this)
        // Idempotent and non-blocking (PRD 8.1): Beranda observes Room reactively and renders as
        // soon as rows exist, so neither bootstrap nor sync scheduling may gate the first frame.
        applicationScope.launch {
            // Nahwu Quiz (`0.0.5`) is the only bundled-asset pipeline left — amaliyah content
            // comes exclusively from the CMS now, and Beranda's own resume sync is what fetches
            // it on the very first launch.
            runCatching { nahwuQuizBootstrapper.bootstrapIfNeeded() }
                .onSuccess { outcome -> logNahwuQuizBootstrapOutcome(outcome) }
                .onFailure { Log.w(TAG, "Nahwu Quiz bootstrap failed", it) }

            // The background window. Beranda also syncs on every resume, so this is the
            // catch-up path for an install that is opened rarely, not the primary one.
            runCatching { contentSyncScheduler.enqueueIfStale() }
                .onFailure { Log.w(TAG, "content sync scheduling failed", it) }

            // Quran differs from the daily amaliyah catalog check: this fetches only a tiny
            // Remote Config control value and enqueues a full unmetered update when its monotonic
            // version is newer than an already-complete local Quran dataset.
            runCatching { quranUpdateScheduler.enqueueIfUpdateAvailable() }
                .onFailure { Log.w(TAG, "Quran version update scheduling failed", it) }

            // Re-arms the next adzan on every cold start. The alarm chain re-arms itself as it
            // fires, so this only matters after the process was killed with nothing pending — but
            // that is also the case where nothing else would ever put it back.
            runCatching { prayerAlarmScheduler.rearm() }
                .onFailure { Log.w(TAG, "prayer alarm rearm failed", it) }
        }
    }

    /** Catalog item images (`Content.imageUrl`): a network-capable Coil `ImageLoader`
     * is opt-in per Coil 3 — without this, [coil3.compose.AsyncImage] can only load local models. */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader
        .Builder(context)
        .components { add(OkHttpNetworkFetcherFactory()) }
        .build()

    private fun logNahwuQuizBootstrapOutcome(outcome: NahwuQuizBootstrapOutcome) {
        when (outcome) {
            is NahwuQuizBootstrapOutcome.AlreadySeeded -> Log.d(TAG, "Nahwu Quiz: already seeded")
            is NahwuQuizBootstrapOutcome.Imported ->
                Log.d(TAG, "Nahwu Quiz: imported ${outcome.packageCount} package(s)")

            is NahwuQuizBootstrapOutcome.Rejected ->
                Log.w(TAG, "Nahwu Quiz bootstrap rejected: ${outcome.reason}")
        }
    }

    private companion object {
        const val TAG = "SanguSantriApplication"
    }
}
