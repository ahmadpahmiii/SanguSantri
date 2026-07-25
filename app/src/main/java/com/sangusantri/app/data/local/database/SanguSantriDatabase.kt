package com.sangusantri.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.entity.AppMetadataEntity

/**
 * Canonical local source of truth (PRD 12.1). Content entities (amaliyah,
 * variant, version, step) and progress entities (reading_sessions,
 * step_progress) are added when the canonical content model and reader are
 * implemented; destructive migrations are prohibited once this ships (PRD 16.1).
 */
@Database(
    entities = [AppMetadataEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SanguSantriDatabase : RoomDatabase() {
    abstract fun appMetadataDao(): AppMetadataDao

    companion object {
        const val DATABASE_NAME = "sangusantri.db"
    }
}
