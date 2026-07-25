package com.sangusantri.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the canonical content hierarchy (amaliyah, amaliyah_variants, approvals,
 * amaliyah_versions, amaliyah_steps). Statements are copied verbatim from the
 * Room-exported schema (`app/schemas/.../2.json`) so the resulting on-disk
 * schema matches what Room expects on next open (ADR 0003: no destructive migration).
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createAmaliyahTable(db)
            createAmaliyahVariantsTable(db)
            createApprovalsTable(db)
            createAmaliyahVersionsTable(db)
            createAmaliyahStepsTable(db)
        }

        private fun createAmaliyahTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `amaliyah` (`id` TEXT NOT NULL, `slug` TEXT NOT NULL, " +
                    "`titleId` TEXT NOT NULL, `titleAr` TEXT NOT NULL, `descriptionId` TEXT, " +
                    "`descriptionAr` TEXT, `category` TEXT NOT NULL, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_amaliyah_slug` ON `amaliyah` (`slug`)",
            )
        }

        private fun createAmaliyahVariantsTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `amaliyah_variants` (`id` TEXT NOT NULL, `amaliyahId` TEXT NOT NULL, " +
                    "`slug` TEXT NOT NULL, `nameId` TEXT NOT NULL, `nameAr` TEXT NOT NULL, " +
                    "`ownerType` TEXT NOT NULL, `pondokId` TEXT, `visibility` TEXT NOT NULL, " +
                    "`isDefault` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`amaliyahId`) REFERENCES " +
                    "`amaliyah`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_amaliyah_variants_amaliyahId` " +
                    "ON `amaliyah_variants` (`amaliyahId`)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_amaliyah_variants_amaliyahId_slug` " +
                    "ON `amaliyah_variants` (`amaliyahId`, `slug`)",
            )
        }

        private fun createApprovalsTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `approvals` (`id` TEXT NOT NULL, `approverName` TEXT NOT NULL, " +
                    "`approverRole` TEXT NOT NULL, `institutionName` TEXT, `approvalDate` TEXT NOT NULL, " +
                    "`approvalScope` TEXT NOT NULL, `publicDocumentStorageKey` TEXT, `documentReferenceNumber` TEXT, " +
                    "`status` TEXT NOT NULL, PRIMARY KEY(`id`))",
            )
        }

        private fun createAmaliyahVersionsTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `amaliyah_versions` (`id` TEXT NOT NULL, `variantId` TEXT NOT NULL, " +
                    "`versionNumber` INTEGER NOT NULL, `schemaVersion` INTEGER NOT NULL, `status` TEXT NOT NULL, " +
                    "`sourceName` TEXT NOT NULL, `sourceReference` TEXT NOT NULL, `approvalId` TEXT NOT NULL, " +
                    "`checksumSha256` TEXT NOT NULL, `minimumAppVersionCode` INTEGER NOT NULL, `publishedAt` TEXT, " +
                    "`revokedAt` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`variantId`) REFERENCES " +
                    "`amaliyah_variants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`approvalId`) " +
                    "REFERENCES `approvals`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_amaliyah_versions_variantId` " +
                    "ON `amaliyah_versions` (`variantId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_amaliyah_versions_approvalId` " +
                    "ON `amaliyah_versions` (`approvalId`)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_amaliyah_versions_variantId_versionNumber` " +
                    "ON `amaliyah_versions` (`variantId`, `versionNumber`)",
            )
        }

        private fun createAmaliyahStepsTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `amaliyah_steps` (`id` TEXT NOT NULL, `versionId` TEXT NOT NULL, " +
                    "`position` INTEGER NOT NULL, `stepType` TEXT NOT NULL, `titleId` TEXT, `titleAr` TEXT, " +
                    "`arabicText` TEXT, `translationId` TEXT, `instructionId` TEXT, `instructionAr` TEXT, " +
                    "`repeatTarget` INTEGER, `quranSurahNumber` INTEGER, `quranAyahStart` INTEGER, " +
                    "`quranAyahEnd` INTEGER, `audioGroupId` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`versionId`) " +
                    "REFERENCES `amaliyah_versions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_amaliyah_steps_versionId` ON `amaliyah_steps` (`versionId`)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_amaliyah_steps_versionId_position` " +
                    "ON `amaliyah_steps` (`versionId`, `position`)",
            )
        }
    }

/**
 * Adds `reading_positions` (Milestone 3 minimum reading-position persistence scope — see
 * [com.sangusantri.app.domain.model.ReadingPosition]). Statement copied verbatim from the
 * Room-exported schema (`app/schemas/.../3.json`) so the resulting on-disk schema matches what
 * Room expects on next open (ADR 0003: no destructive migration).
 */
val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `reading_positions` (`versionId` TEXT NOT NULL, " +
                        "`itemIndex` INTEGER NOT NULL, `itemOffset` INTEGER NOT NULL, " +
                        "`lastOpenedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`versionId`))",
            )
        }
    }
