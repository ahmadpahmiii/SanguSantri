package com.sangusantri.app.di

import com.sangusantri.app.data.repository.TasbihRepositoryImpl
import com.sangusantri.app.domain.repository.TasbihRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TasbihModule {
    @Binds
    abstract fun bindTasbihRepository(impl: TasbihRepositoryImpl): TasbihRepository
}
