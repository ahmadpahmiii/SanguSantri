package com.sangusantri.app.di

import com.sangusantri.app.data.repository.ReminderRepositoryImpl
import com.sangusantri.app.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
