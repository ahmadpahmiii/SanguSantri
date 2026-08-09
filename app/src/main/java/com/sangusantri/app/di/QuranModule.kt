package com.sangusantri.app.di

import com.sangusantri.app.data.repository.QuranReaderSettingsRepositoryImpl
import com.sangusantri.app.data.repository.QuranRepositoryImpl
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuranModule {
    @Binds
    @Singleton
    abstract fun bindQuranRepository(impl: QuranRepositoryImpl): QuranRepository

    @Binds
    @Singleton
    abstract fun bindQuranReaderSettingsRepository(
        impl: QuranReaderSettingsRepositoryImpl,
    ): QuranReaderSettingsRepository
}
