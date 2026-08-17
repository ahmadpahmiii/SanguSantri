package com.sangusantri.app.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor
import com.sangusantri.app.data.remote.prayertimes.api.PrayerTimesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Distinguishes the myquran client from [NetworkModule]'s content client and [QuranNetworkModule]'s
 * credentialed Kemenag one. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrayerTimesHttpClient

/**
 * The myquran network stack — a third, deliberately **unauthenticated** client.
 *
 * It carries no interceptor that could attach a credential: myquran needs none, and the Kemenag
 * credential must never reach a non-Kemenag origin (ADR 0016 §5). Keeping the three clients apart
 * is what makes that structural rather than a convention someone has to remember.
 */
@Module
@InstallIn(SingletonComponent::class)
object PrayerTimesNetworkModule {
    @Provides
    @Singleton
    @PrayerTimesHttpClient
    fun providePrayerTimesOkHttpClient(chuckerInterceptor: ChuckerInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(ResponseSizeLimitInterceptor())
            .addInterceptor(chuckerInterceptor)
            .build()

    @Provides
    @Singleton
    fun providePrayerTimesApiService(
        @PrayerTimesHttpClient okHttpClient: OkHttpClient,
    ): PrayerTimesApiService {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.PRAYER_TIMES_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PrayerTimesApiService::class.java)
    }

    private const val NETWORK_TIMEOUT_SECONDS = 15L
}
