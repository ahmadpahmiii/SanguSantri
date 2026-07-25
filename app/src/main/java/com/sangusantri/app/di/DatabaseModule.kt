package com.sangusantri.app.di

import android.content.Context
import androidx.room.Room
import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.local.database.MIGRATION_1_2
import com.sangusantri.app.data.local.database.MIGRATION_2_3
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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideAppMetadataDao(database: SanguSantriDatabase): AppMetadataDao = database.appMetadataDao()

    @Provides
    fun provideAmaliyahDao(database: SanguSantriDatabase): AmaliyahDao = database.amaliyahDao()

    @Provides
    fun provideAmaliyahVariantDao(database: SanguSantriDatabase): AmaliyahVariantDao = database.amaliyahVariantDao()

    @Provides
    fun provideApprovalDao(database: SanguSantriDatabase): ApprovalDao = database.approvalDao()

    @Provides
    fun provideAmaliyahVersionDao(database: SanguSantriDatabase): AmaliyahVersionDao = database.amaliyahVersionDao()

    @Provides
    fun provideAmaliyahStepDao(database: SanguSantriDatabase): AmaliyahStepDao = database.amaliyahStepDao()

    @Provides
    fun provideReadingPositionDao(database: SanguSantriDatabase): ReadingPositionDao = database.readingPositionDao()
}
