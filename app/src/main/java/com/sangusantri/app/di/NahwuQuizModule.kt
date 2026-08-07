package com.sangusantri.app.di

import com.sangusantri.app.data.repository.NahwuQuizRepositoryImpl
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NahwuQuizModule {
    @Binds
    abstract fun bindNahwuQuizRepository(impl: NahwuQuizRepositoryImpl): NahwuQuizRepository
}
