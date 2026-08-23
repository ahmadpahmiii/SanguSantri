package com.sangusantri.app.di

import com.sangusantri.app.data.repository.AyatHariIniRepositoryImpl
import com.sangusantri.app.domain.repository.AyatHariIniRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * `AyatHariIniRemoteSource` is no longer bound here. It was an interface with a fixture
 * implementation only for as long as the CMS endpoint did not exist; it is now a plain injectable
 * class that Hilt constructs from its `@Inject` constructor, so there is nothing left to choose
 * between.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AyatHariIniModule {
    @Binds
    @Singleton
    abstract fun bindAyatHariIniRepository(impl: AyatHariIniRepositoryImpl): AyatHariIniRepository
}
