package com.sangusantri.app.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor
import com.sangusantri.app.data.remote.api.ContentApiService
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(chuckerInterceptor: ChuckerInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Matches the Quran, myquran, and murottal clients. ContentValidator pins every
            // contentUrl to an origin-relative path so content can only come from the configured
            // content API origin; following a redirect would hand that decision back to
            // whatever the host answered with, defeating the pin.
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(ResponseSizeLimitInterceptor())
            .addInterceptor(chuckerInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideContentJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.CONTENT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideContentApiService(retrofit: Retrofit): ContentApiService = retrofit.create(ContentApiService::class.java)

    private const val NETWORK_TIMEOUT_SECONDS = 15L
}
