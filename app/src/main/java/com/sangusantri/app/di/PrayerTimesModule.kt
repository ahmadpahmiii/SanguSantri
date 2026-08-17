package com.sangusantri.app.di

import com.sangusantri.app.data.repository.KiblatRepositoryImpl
import com.sangusantri.app.data.repository.PrayerScheduleRepositoryImpl
import com.sangusantri.app.domain.repository.KiblatRepository
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Jadwal Sholat's repository binding. The DAO comes from [DatabaseModule] and the API service from
 * [PrayerTimesNetworkModule]. */
@Module
@InstallIn(SingletonComponent::class)
abstract class PrayerTimesModule {
    @Binds
    abstract fun bindPrayerScheduleRepository(impl: PrayerScheduleRepositoryImpl): PrayerScheduleRepository

    @Binds
    abstract fun bindKiblatRepository(impl: KiblatRepositoryImpl): KiblatRepository
}
