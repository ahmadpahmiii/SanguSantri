package com.sangusantri.app.di

import android.content.Context
import androidx.room.Room
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideSanguSantriDatabase(
        @ApplicationContext context: Context,
    ): SanguSantriDatabase =
        Room
            .databaseBuilder(
                context,
                SanguSantriDatabase::class.java,
                SanguSantriDatabase.DATABASE_NAME,
            ).build()

    @Provides
    fun provideAppMetadataDao(database: SanguSantriDatabase): AppMetadataDao = database.appMetadataDao()
}
