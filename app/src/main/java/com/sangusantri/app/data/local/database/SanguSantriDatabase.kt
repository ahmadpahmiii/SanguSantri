package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.AyatHariIniDao
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
import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import com.sangusantri.app.data.local.entity.AyatHariIniEntity
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.NahwuQuizAttemptEntity
import com.sangusantri.app.data.local.entity.NahwuQuizPackageEntity
import com.sangusantri.app.data.local.entity.NahwuQuizQuestionEntity
import com.sangusantri.app.data.local.entity.PrayerCityEntity
import com.sangusantri.app.data.local.entity.PrayerScheduleDayEntity
import com.sangusantri.app.data.local.entity.QuranBookmarkEntity
import com.sangusantri.app.data.local.entity.QuranReadingSessionEntity
import com.sangusantri.app.data.local.entity.QuranReadingStateEntity
import com.sangusantri.app.data.local.entity.QuranSurahEntity
import com.sangusantri.app.data.local.entity.QuranTafsirEntity
import com.sangusantri.app.data.local.entity.QuranVerseEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.data.local.entity.ReminderEntity
import com.sangusantri.app.data.local.entity.StepProgressEntity
import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import com.sangusantri.app.data.local.entity.TasbihSessionEntity

/**
 * Canonical local source of truth (PRD 12.1).
 *
 * **Every schema change wipes this database.** The builder keeps
 * `fallbackToDestructiveMigration(dropAllTables = true)` and there is no migration chain — that
 * one line is the entire policy, up and down. There are no production installs to protect, so a
 * migration would be unrequested work; revisit only when there are.
 *
 * The rule that matters when changing an entity: **bump `version` in the same commit**. Room
 * compares a schema hash on open and throws *"Room cannot verify the data integrity"* on a
 * changed-but-unbumped schema, which destructive fallback does not rescue. Bumping is what turns
 * that crash into a silent wipe. A per-version log is deliberately not kept here — since no
 * version ever migrates, the only thing a reader needs is the sentence above, and `git log` has
 * the rest.
 *
 * What a wipe costs the reader: the CMS amaliyah catalogue re-syncs on the next launch, but
 * downloaded Quran text/tafsir, Quran bookmarks, tasbih history, amaliyah progress and completion
 * events, reminders, and quiz attempts are gone. Downloaded murottal audio survives — it lives as
 * files under `filesDir/murottal/`, deliberately outside Room for exactly this reason. Data that
 * must survive a wipe belongs there, not here.
 */
@Database(
    entities = [
        AppMetadataEntity::class,
        ContentEntity::class,
        ContentStepEntity::class,
        ReadingPositionEntity::class,
        GuidedReadingSessionEntity::class,
        StepProgressEntity::class,
        TasbihSessionEntity::class,
        TasbihHistoryEntity::class,
        AmaliyahCompletionEventEntity::class,
        ReminderEntity::class,
        NahwuQuizPackageEntity::class,
        NahwuQuizQuestionEntity::class,
        NahwuQuizAttemptEntity::class,
        QuranSurahEntity::class,
        QuranVerseEntity::class,
        QuranTafsirEntity::class,
        QuranBookmarkEntity::class,
        QuranReadingStateEntity::class,
        QuranReadingSessionEntity::class,
        PrayerCityEntity::class,
        PrayerScheduleDayEntity::class,
        AyatHariIniEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
// One abstract getter per Room DAO is the natural, unavoidable shape of a Room @Database class.
@Suppress("TooManyFunctions")
abstract class SanguSantriDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun ayatHariIniDao(): AyatHariIniDao

    abstract fun contentDao(): ContentDao

    abstract fun contentStepDao(): ContentStepDao

    abstract fun readingPositionDao(): ReadingPositionDao

    abstract fun guidedReadingSessionDao(): GuidedReadingSessionDao

    abstract fun stepProgressDao(): StepProgressDao

    abstract fun tasbihSessionDao(): TasbihSessionDao

    abstract fun tasbihHistoryDao(): TasbihHistoryDao

    abstract fun amaliyahCompletionEventDao(): AmaliyahCompletionEventDao

    abstract fun reminderDao(): ReminderDao

    abstract fun nahwuQuizPackageDao(): NahwuQuizPackageDao

    abstract fun nahwuQuizQuestionDao(): NahwuQuizQuestionDao

    abstract fun nahwuQuizAttemptDao(): NahwuQuizAttemptDao

    abstract fun quranSurahDao(): QuranSurahDao

    abstract fun quranVerseDao(): QuranVerseDao

    abstract fun quranTafsirDao(): QuranTafsirDao

    abstract fun quranBookmarkDao(): QuranBookmarkDao

    abstract fun quranReadingStateDao(): QuranReadingStateDao

    abstract fun quranReadingSessionDao(): QuranReadingSessionDao

    abstract fun prayerTimesDao(): PrayerTimesDao

    companion object {
        const val DATABASE_NAME = "sangusantri.db"
    }
}
