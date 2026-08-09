package com.sangusantri.app.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Shared in-app HTTP inspector (debug only — `library-no-op` in release makes every call here a
 * no-op with the same API, so [NetworkModule] and [QuranNetworkModule] never branch on build
 * type). One [ChuckerInterceptor] is added to both OkHttp clients so all REST traffic — content
 * sync and Kemenag — shows up in Chucker's notification/UI.
 *
 * `redactHeaders` covers `Authorization`/`user` per `docs/product/QURAN_PRD.md` §9's explicit
 * "redact both header names/values from all logging and test interceptors" requirement — this
 * inspector must never display the real Kemenag credential, even though it never leaves the
 * device and only exists in debug builds.
 */
@Module
@InstallIn(SingletonComponent::class)
object ChuckerModule {
    @Provides
    @Singleton
    fun provideChuckerCollector(
        @ApplicationContext context: Context,
    ): ChuckerCollector = ChuckerCollector(context)

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context,
        collector: ChuckerCollector,
    ): ChuckerInterceptor =
        ChuckerInterceptor
            .Builder(context)
            .collector(collector)
            .redactHeaders("Authorization", "user")
            .build()
}
