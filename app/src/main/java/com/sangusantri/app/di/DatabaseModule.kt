package com.sangusantri.app.di

import android.content.Context
import androidx.room.Room
import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.local.dao.GuidedReadingSessionDao
import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.local.dao.StepProgressDao
import com.sangusantri.app.data.local.dao.TasbihHistoryDao
import com.sangusantri.app.data.local.dao.TasbihSessionDao
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// One @Provides function per Room DAO is the established convention in this file (mirrors
// SanguSantriDatabase's own one-abstract-getter-per-DAO shape) — splitting it would mean an
// artificial second module for no boundary reason, which CODING_STANDARD.md also warns against.
@Suppress("TooManyFunctions")
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

    @Provides
    fun provideGuidedReadingSessionDao(database: SanguSantriDatabase): GuidedReadingSessionDao =
        database.guidedReadingSessionDao()

    @Provides
    fun provideStepProgressDao(database: SanguSantriDatabase): StepProgressDao = database.stepProgressDao()

    @Provides
    fun provideTasbihSessionDao(database: SanguSantriDatabase): TasbihSessionDao = database.tasbihSessionDao()

    @Provides
    fun provideTasbihHistoryDao(database: SanguSantriDatabase): TasbihHistoryDao = database.tasbihHistoryDao()

    @Provides
    fun provideAmaliyahCompletionEventDao(database: SanguSantriDatabase): AmaliyahCompletionEventDao =
        database.amaliyahCompletionEventDao()
}
