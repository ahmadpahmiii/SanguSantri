package com.sangusantri.app.di

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.repository.AppUpdatePolicyRepositoryImpl
import com.sangusantri.app.domain.repository.AppUpdatePolicyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateModule {
    @Binds
    @Singleton
    abstract fun bindAppUpdatePolicyRepository(impl: AppUpdatePolicyRepositoryImpl): AppUpdatePolicyRepository
}

/**
 * Debug builds fetch on every call (`minimumFetchIntervalInSeconds = 0`) rather than Remote
 * Config's 12-hour default (Android does not auto-lower this for debug builds) — otherwise console
 * changes would appear to silently not take effect locally for up to 12 hours.
 */
@Module
@InstallIn(SingletonComponent::class)
object UpdateProvidersModule {
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else RELEASE_FETCH_INTERVAL_SECONDS
            },
        )
        return remoteConfig
    }

    @Provides
    @Singleton
    fun provideAppUpdateManager(
        @ApplicationContext context: Context,
    ): AppUpdateManager = AppUpdateManagerFactory.create(context)

    private const val RELEASE_FETCH_INTERVAL_SECONDS = 3_600L
}
