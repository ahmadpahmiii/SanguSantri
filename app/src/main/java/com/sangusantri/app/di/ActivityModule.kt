package com.sangusantri.app.di

import com.sangusantri.app.data.repository.ActivityRepositoryImpl
import com.sangusantri.app.data.repository.AmalanRepositoryImpl
import com.sangusantri.app.domain.repository.ActivityRepository
import com.sangusantri.app.domain.repository.AmalanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityModule {
    @Binds
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindAmalanRepository(impl: AmalanRepositoryImpl): AmalanRepository
}
