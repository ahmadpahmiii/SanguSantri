package com.sangusantri.app.di

import com.sangusantri.app.data.repository.ContentRepositoryImpl
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {
    /**
     * `@Singleton` is load-bearing, not decoration: this binding was unscoped, so Hilt built a
     * fresh repository for every ViewModel that injected one. That is wrong for a repository in
     * general, and specifically it would defeat [ContentRepositoryImpl]'s shared-refresh
     * de-duplication — per-instance state cannot de-duplicate across instances.
     */
    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
