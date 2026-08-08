package com.sangusantri.app.di

import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor
import com.sangusantri.app.data.remote.quran.QuranAuthInterceptor
import com.sangusantri.app.data.remote.quran.api.QuranApiService
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

/** Distinguishes the Quran-dedicated [OkHttpClient] from [NetworkModule]'s shared content client. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QuranHttpClient

/**
 * The Quran-dedicated network stack (ADR 0016) — wholly separate from [NetworkModule]'s Firebase
 * Hosting content client, so a Kemenag credential can never attach to a non-Kemenag request.
 */
@Module
@InstallIn(SingletonComponent::class)
object QuranNetworkModule {
    @Provides
    @Singleton
    @QuranHttpClient
    fun provideQuranOkHttpClient(authInterceptor: QuranAuthInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(ResponseSizeLimitInterceptor())
            .build()

    @Provides
    @Singleton
    fun provideQuranApiService(
        @QuranHttpClient okHttpClient: OkHttpClient,
    ): QuranApiService {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.QURAN_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(QuranApiService::class.java)
    }

    private const val NETWORK_TIMEOUT_SECONDS = 15L
}
