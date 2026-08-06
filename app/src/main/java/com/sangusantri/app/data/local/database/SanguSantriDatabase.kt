package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sangusantri.app.data.local.dao.AmaliyahCompletionEventDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.local.dao.GuidedReadingSessionDao
import com.sangusantri.app.data.local.dao.ReadingPositionDao
import com.sangusantri.app.data.local.dao.StepProgressDao
import com.sangusantri.app.data.local.dao.TasbihHistoryDao
import com.sangusantri.app.data.local.dao.TasbihSessionDao
import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
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
 * (`TasbihSessionEntity.targetPreset`), so no `Converters` class is needed any more. Destructive
 * migration (`fallbackToDestructiveMigration`) is deliberately NOT used.
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
    ],
    version = 2,
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

    companion object {
        const val DATABASE_NAME = "sangusantri.db"
    }
}
