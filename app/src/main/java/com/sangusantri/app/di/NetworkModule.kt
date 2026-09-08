package com.sangusantri.app.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor
import com.sangusantri.app.data.remote.api.ContentApiService
import com.sangusantri.app.data.remote.ayat.AyatHariIniApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * The HTTP response cache is what makes content sync conditional.
     *
     * The CMS content endpoints send a strong `ETag` and
     * `Cache-Control: public, max-age=0, must-revalidate`, so every sync revalidates and an
     * unchanged category comes back `304` with no body. OkHttp stores the validator and replays it
     * as `If-None-Match` on its own — there is no ETag handling anywhere in this app's own code,
     * and deliberately so: the previous attempt at manifest-level ETag plumbing was removed for
     * being hand-rolled (ADR 0012 amendment, 2026-07-28), and this puts the same saving back
     * where the HTTP client already implements it.
     *
     * `max-age=0` means nothing is ever served from this cache without asking the server first, so
     * it cannot mask a content update — it only avoids re-downloading one. Offline behaviour does
     * not depend on it either: the reader reads from Room, not from here.
     */
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache =
        Cache(File(context.cacheDir, HTTP_CACHE_DIR), HTTP_CACHE_BYTES)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        chuckerInterceptor: ChuckerInterceptor,
        @DebugInterceptor debugInterceptors: Set<@JvmSuppressWildcards Interceptor>,
        cache: Cache,
    ): OkHttpClient = OkHttpClient
        .Builder()
        .cache(cache)
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
        .apply {
            debugInterceptors.forEach { addInterceptor(it) }
        }.build()

    @Provides
    @Singleton
    fun provideContentJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit
        .Builder()
        .baseUrl(BuildConfig.CONTENT_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideContentApiService(retrofit: Retrofit): ContentApiService = retrofit.create(ContentApiService::class.java)

    // Same Retrofit instance, same origin, same read-only contract as the catalog: the quote of
    // the day is served by the CMS Content API alongside everything else, so it needs no client of
    // its own (unlike Quran, which is a different host and carries credentials).
    @Provides
    @Singleton
    fun provideAyatHariIniApiService(retrofit: Retrofit): AyatHariIniApiService =
        retrofit.create(AyatHariIniApiService::class.java)

    private const val NETWORK_TIMEOUT_SECONDS = 15L
    private const val HTTP_CACHE_DIR = "http-cache"

    // Both content categories fully published are ~190 KB gzipped; 5 MB leaves room for the
    // quote-of-the-day window and Quran/prayer-time responses without ever being the reason a
    // revalidation misses.
    private const val HTTP_CACHE_BYTES = 5L * 1024 * 1024
}
