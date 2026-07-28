package com.sangusantri.app.di

import com.sangusantri.app.data.repository.ContentRepositoryImpl
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {
    @Binds
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
