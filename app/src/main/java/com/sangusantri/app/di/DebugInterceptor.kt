package com.sangusantri.app.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for OkHttp interceptors that should only be added in debug builds.
 *
 * Used with multibindings to decouple [NetworkModule] from debug-only interceptor
 * implementations like HttpLoggingInterceptor.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugInterceptor
