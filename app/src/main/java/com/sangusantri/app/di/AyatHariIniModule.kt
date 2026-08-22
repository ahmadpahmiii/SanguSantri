package com.sangusantri.app.di

import com.sangusantri.app.data.remote.ayat.AyatHariIniRemoteSource
import com.sangusantri.app.data.remote.ayat.FixtureAyatHariIniRemoteSource
import com.sangusantri.app.data.repository.AyatHariIniRepositoryImpl
import com.sangusantri.app.domain.repository.AyatHariIniRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AyatHariIniModule {
    @Binds
    @Singleton
    abstract fun bindAyatHariIniRepository(impl: AyatHariIniRepositoryImpl): AyatHariIniRepository

    /**
     * **The one line to change when the CMS endpoint ships.** Swap
     * [FixtureAyatHariIniRemoteSource] for `ApiAyatHariIniRemoteSource` (and add
     * `AyatHariIniApiService` to `NetworkModule`, alongside `ContentApiService`). Everything else in
     * this feature is already talking to the real interface.
     */
    @Binds
    @Singleton
    abstract fun bindAyatHariIniRemoteSource(impl: FixtureAyatHariIniRemoteSource): AyatHariIniRemoteSource
}
