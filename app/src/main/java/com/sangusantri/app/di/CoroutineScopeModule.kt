package com.sangusantri.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** A [CoroutineScope] that lives as long as the process. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * Work that must outlive whoever asked for it.
 *
 * The motivating case is a shared refresh: when two screens ask a repository to refresh at the same
 * moment, the second joins the first rather than starting its own request. That only holds if the
 * shared work is not tied to either caller's scope — otherwise the first screen going away cancels a
 * refresh the second is still waiting on.
 *
 * [SupervisorJob] so one failed refresh never cancels the scope for everything else.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
