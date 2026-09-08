package com.sangusantri.app.di

import com.sangusantri.app.core.telemetry.LoggingInitializer
import com.sangusantri.app.core.telemetry.LoggingInitializerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugLoggingModule {
    @Binds
    @Singleton
    abstract fun bindLoggingInitializer(impl: LoggingInitializerImpl): LoggingInitializer

    companion object {
        @Provides
        @Singleton
        @DebugInterceptor
        @IntoSet
        fun provideHttpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}
