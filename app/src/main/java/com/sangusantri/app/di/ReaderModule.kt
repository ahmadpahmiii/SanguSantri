package com.sangusantri.app.di

import com.sangusantri.app.data.repository.GuidedReadingRepositoryImpl
import com.sangusantri.app.data.repository.ReaderSettingsRepositoryImpl
import com.sangusantri.app.data.repository.ReadingPositionRepositoryImpl
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {
    @Binds
    abstract fun bindReadingPositionRepository(impl: ReadingPositionRepositoryImpl): ReadingPositionRepository

    @Binds
    abstract fun bindReaderSettingsRepository(impl: ReaderSettingsRepositoryImpl): ReaderSettingsRepository

    @Binds
    abstract fun bindGuidedReadingRepository(impl: GuidedReadingRepositoryImpl): GuidedReadingRepository
}
