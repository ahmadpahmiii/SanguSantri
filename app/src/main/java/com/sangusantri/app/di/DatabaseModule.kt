package com.sangusantri.app.di

import android.content.Context
import androidx.room.Room
import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.local.dao.GuidedReadingSessionDao
import com.sangusantri.app.data.local.dao.NahwuQuizAttemptDao
import com.sangusantri.app.data.local.dao.NahwuQuizPackageDao
import com.sangusantri.app.data.local.dao.NahwuQuizQuestionDao
import com.sangusantri.app.data.local.dao.PrayerTimesDao
import com.sangusantri.app.data.local.dao.QuranBookmarkDao
import com.sangusantri.app.data.local.dao.QuranReadingSessionDao
import com.sangusantri.app.data.local.dao.QuranReadingStateDao
import com.sangusantri.app.data.local.dao.QuranSurahDao
import com.sangusantri.app.data.local.dao.QuranTafsirDao
import com.sangusantri.app.data.local.dao.QuranVerseDao
import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.local.dao.ReminderDao
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
            ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePrayerTimesDao(database: SanguSantriDatabase): PrayerTimesDao = database.prayerTimesDao()

    @Provides
    fun provideAppMetadataDao(database: SanguSantriDatabase): AppMetadataDao = database.appMetadataDao()

    @Provides
    fun provideContentDao(database: SanguSantriDatabase): ContentDao = database.contentDao()

    @Provides
    fun provideContentStepDao(database: SanguSantriDatabase): ContentStepDao = database.contentStepDao()

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

    @Provides
    fun provideReminderDao(database: SanguSantriDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideNahwuQuizPackageDao(database: SanguSantriDatabase): NahwuQuizPackageDao = database.nahwuQuizPackageDao()

    @Provides
    fun provideNahwuQuizQuestionDao(database: SanguSantriDatabase): NahwuQuizQuestionDao =
        database.nahwuQuizQuestionDao()

    @Provides
    fun provideNahwuQuizAttemptDao(database: SanguSantriDatabase): NahwuQuizAttemptDao = database.nahwuQuizAttemptDao()

    @Provides
    fun provideQuranSurahDao(database: SanguSantriDatabase): QuranSurahDao = database.quranSurahDao()

    @Provides
    fun provideQuranVerseDao(database: SanguSantriDatabase): QuranVerseDao = database.quranVerseDao()

    @Provides
    fun provideQuranTafsirDao(database: SanguSantriDatabase): QuranTafsirDao = database.quranTafsirDao()

    @Provides
    fun provideQuranBookmarkDao(database: SanguSantriDatabase): QuranBookmarkDao = database.quranBookmarkDao()

    @Provides
    fun provideQuranReadingStateDao(database: SanguSantriDatabase): QuranReadingStateDao =
        database.quranReadingStateDao()

    @Provides
    fun provideQuranReadingSessionDao(database: SanguSantriDatabase): QuranReadingSessionDao =
        database.quranReadingSessionDao()
}
