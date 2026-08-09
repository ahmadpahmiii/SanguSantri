package com.sangusantri.app.di

import com.sangusantri.app.data.repository.HomePreferencesRepositoryImpl
import com.sangusantri.app.domain.repository.HomePreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun bindHomePreferencesRepository(impl: HomePreferencesRepositoryImpl): HomePreferencesRepository
}
