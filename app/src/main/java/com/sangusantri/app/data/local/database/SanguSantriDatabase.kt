package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.AppMetadataEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity

/**
 * Canonical local source of truth (PRD 12.1). Progress entities
 * (reading_sessions, step_progress) are added when the reader is implemented;
 * destructive migrations are prohibited once this ships (PRD 16.1, ADR 0003).
 */
@Database(
    entities = [
        AppMetadataEntity::class,
        AmaliyahEntity::class,
        AmaliyahVariantEntity::class,
        ApprovalEntity::class,
        AmaliyahVersionEntity::class,
        AmaliyahStepEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SanguSantriDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

    abstract fun amaliyahDao(): AmaliyahDao

    abstract fun amaliyahVariantDao(): AmaliyahVariantDao

    abstract fun approvalDao(): ApprovalDao

    abstract fun amaliyahVersionDao(): AmaliyahVersionDao

    abstract fun amaliyahStepDao(): AmaliyahStepDao

    companion object {
        const val DATABASE_NAME = "sangusantri.db"
    }
}
