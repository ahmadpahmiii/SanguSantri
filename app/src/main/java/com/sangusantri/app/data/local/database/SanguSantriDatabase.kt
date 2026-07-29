package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.sangusantri.app.data.local.entity.AmaliyahCompletionEventEntity
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity
import com.sangusantri.app.data.local.entity.GuidedReadingSessionEntity
import com.sangusantri.app.data.local.entity.ReadingPositionEntity
import com.sangusantri.app.data.local.entity.StepProgressEntity
import com.sangusantri.app.data.local.entity.TasbihHistoryEntity
import com.sangusantri.app.data.local.entity.TasbihSessionEntity

/**
 * Canonical local source of truth (PRD 12.1). Pre-public-release schema baseline, reset to
 * version 1 at Milestone 4 (`docs/engineering/CONTENT_MODEL.md` schema-freeze policy) — the app
 * has no released production users, so the Milestone 1-3 migration chain (`MIGRATION_1_2`,
 * `MIGRATION_2_3`) was consolidated into this single coherent schema rather than carried forward.
 * Developers with an existing local install must clear app data or reinstall once after pulling
 * this change (Room cannot open an on-disk database at a higher version number than this one
 * declares). Destructive migration (`fallbackToDestructiveMigration`) is deliberately NOT used
 * here or anywhere else — real Room migrations become mandatory again once the initial public
 * schema is frozen (ADR 0003).
 *
 * `TasbihSessionEntity`/`TasbihHistoryEntity` (0.0.2 Standalone Tasbih) were added directly to this
 * same version-1 baseline rather than bumping to version 2, per `CONTENT_MODEL.md`'s schema-freeze
 * policy — developers with an existing local install must clear app data or reinstall once again.
 * `AmaliyahCompletionEventEntity` (0.0.3 Aktivitas) was added the same way; `GuidedReadingSessionEntity`
 * also gained a new `startedAtEpochMillis` column in the same pass.
 */
@Database(
    entities = [
        AppMetadataEntity::class,
        AmaliyahEntity::class,
        AmaliyahVariantEntity::class,
        ApprovalEntity::class,
        AmaliyahVersionEntity::class,
        AmaliyahStepEntity::class,
        ReadingPositionEntity::class,
        GuidedReadingSessionEntity::class,
        StepProgressEntity::class,
        TasbihSessionEntity::class,
        TasbihHistoryEntity::class,
        AmaliyahCompletionEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
// One abstract getter per Room DAO is the natural, unavoidable shape of a Room @Database class.
@Suppress("TooManyFunctions")
@TypeConverters(Converters::class)
abstract class SanguSantriDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun amaliyahDao(): AmaliyahDao

    abstract fun amaliyahVariantDao(): AmaliyahVariantDao

    abstract fun approvalDao(): ApprovalDao

    abstract fun amaliyahVersionDao(): AmaliyahVersionDao

    abstract fun amaliyahStepDao(): AmaliyahStepDao

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
