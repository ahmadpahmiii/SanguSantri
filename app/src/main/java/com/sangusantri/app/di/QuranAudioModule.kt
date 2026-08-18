package com.sangusantri.app.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.sangusantri.app.data.audio.QuranAudioSource
import com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Distinguishes the murottal CDN client from the content, Kemenag, and myquran-API clients. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QuranAudioHttpClient

/**
 * A fourth deliberately **unauthenticated** client, for `cdn.myquran.com` audio only.
 *
 * It exists separately for the same structural reason as [PrayerTimesNetworkModule]: the Kemenag
 * credential must never reach a non-Kemenag origin (ADR 0016 §5), and a client that has no auth
 * interceptor at all cannot leak one by accident. Its own size cap is per-ayah rather than the
 * shared 5 MB content default, and its read timeout is longer because an ayah body is much larger
 * than a JSON response.
 */
@Module
@InstallIn(SingletonComponent::class)
object QuranAudioModule {
    @Provides
    @Singleton
    @QuranAudioHttpClient
    fun provideQuranAudioOkHttpClient(chuckerInterceptor: ChuckerInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(ResponseSizeLimitInterceptor(QuranAudioSource.MAX_AYAH_AUDIO_BYTES))
            .addInterceptor(chuckerInterceptor)
            .build()

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 60L
}
