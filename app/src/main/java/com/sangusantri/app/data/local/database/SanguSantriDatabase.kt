package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.local.dao.GuidedReadingSessionDao
import com.sangusantri.app.data.local.dao.NahwuQuizAttemptDao
import com.sangusantri.app.data.local.dao.NahwuQuizPackageDao
import com.sangusantri.app.data.local.dao.NahwuQuizQuestionDao
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
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.NahwuQuizAttemptEntity
import com.sangusantri.app.data.local.entity.NahwuQuizPackageEntity
import com.sangusantri.app.data.local.entity.NahwuQuizQuestionEntity
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
 * Canonical local source of truth (PRD 12.1). Version 2 (ADR 0015, dynamic catalog
 * simplification) collapses the former Amaliyah/AmaliyahVariant/AmaliyahVersion/AmaliyahStep/
 * Approval hierarchy into the flat `content`/`content_steps` model — see [MIGRATION_1_2] for the
 * real, non-destructive data migration. Enum type converters were removed along with the enums
 * (`StepType`, `AmaliyahVersionStatus`, `ApprovalStatus`, `OwnerType`, `Visibility`) they existed
 * for; Room's built-in enum support (2.6+) is used natively where an enum column still exists
 * (`TasbihSessionEntity.targetPreset`, `NahwuQuizQuestionEntity.correctOption`), so no `Converters`
 * class is needed any more. Destructive migration (`fallbackToDestructiveMigration`) is
 * deliberately NOT used.
 *
 * Version 3 (`0.0.4`, Pengingat Amaliyah) adds `reminders` — purely additive, no existing table
 * changed. `CLAUDE.md`'s temporary design-phase constraint (that pass's Phase E) prohibited writing
 * a new migration class for it, so there is no `MIGRATION_2_3`.
 *
 * Version 4 (`0.0.5`, Nahwu Quiz) adds `nahwu_quiz_packages`/`nahwu_quiz_questions`/
 * `nahwu_quiz_attempts` — also purely additive, and also no `MIGRATION_3_4`: `0.0.5` falls outside
 * `docs/design/DESIGN_HANDOFF.md`'s Phase A–E window (Phase E ends at `0.0.4`), so this follows the
 * project's general, non-temporary pre-release policy instead
 * (`docs/engineering/CONTENT_MODEL.md` "Schema-freeze policy") — which reaches the same outcome: a
 * clean baseline reset, not a real migration, since there are still no production installs to
 * protect. An existing local install must clear app data or reinstall once.
 *
 * Version 5 (`0.0.6`, standalone Al-Qur'an Kemenag) adds `quran_surahs`/`quran_verses`/
 * `quran_tafsir`/`quran_bookmarks`/`quran_reading_state`/`quran_reading_sessions` — a separate
 * bounded context from the amaliyah content model (ADR 0016), purely additive, no `MIGRATION_4_5`
 * for the same pre-release schema-freeze reason as version 4.
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
    ],
    version = 5,
    exportSchema = true,
)
// One abstract getter per Room DAO is the natural, unavoidable shape of a Room @Database class.
@Suppress("TooManyFunctions")
abstract class SanguSantriDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

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

    companion object {
        const val DATABASE_NAME = "sangusantri.db"
    }
}
