package com.sangusantri.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Collapses the former Amaliyah/AmaliyahVariant/AmaliyahVersion/AmaliyahStep/Approval hierarchy
 * into the flat `content`/`content_steps` model (ADR 0015, dynamic catalog simplification). Each
 * amaliyah's default variant's latest published version becomes one `content` row (keyed by the
 * amaliyah's `slug`, matching the new catalog contract's `id`); its steps become `content_steps`
 * rows, renumbered sequentially. `reading_positions`/`guided_reading_sessions`/`step_progress` are
 * re-keyed from `versionId` to that same content id via a join against the (still-present, not yet
 * dropped) old tables — no destructive migration, no data reset.
 *
 * Content metadata fields with no old-schema source (`imageUrl`, `order`, `isActive`) get a
 * placeholder (`NULL`/`0`/`1`) — this is safe because `BundledContentBootstrapper` unconditionally
 * refreshes every catalog item's metadata on the very next app launch, regardless of version, so
 * the placeholder never survives past the first post-migration bootstrap.
 *
 * Real Arabic text and translations are copied verbatim from the existing rows — never invented,
 * never reworded. Per product-owner decision, section-heading-only steps (`stepType = 'HEADING'`,
 * real Indonesian title text but no Arabic body — the new step contract has no field for that) are
 * dropped rather than fabricating Arabic text for them; surviving steps are renumbered so
 * `position` stays a dense 1..N sequence with no gaps.
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createNewTables(db)
            migrateAmaliyahToContent(db)
            remapReadingPositions(db)
            remapGuidedReadingSessions(db)
            remapStepProgress(db)
            dropOldTables(db)
        }

        private fun createNewTables(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `content` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `category` TEXT,
                    `version` INTEGER NOT NULL,
                    `order` INTEGER NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    `sourceName` TEXT NOT NULL,
                    `sourceUrl` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `content_steps` (
                    `id` TEXT NOT NULL,
                    `contentId` TEXT NOT NULL,
                    `position` INTEGER NOT NULL,
                    `arabicText` TEXT NOT NULL,
                    `translation` TEXT NOT NULL,
                    `repeatTarget` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`contentId`) REFERENCES `content`(`id`) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_content_steps_contentId` ON `content_steps` (`contentId`)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_content_steps_contentId_position` " +
                    "ON `content_steps` (`contentId`, `position`)",
            )
        }

        private fun migrateAmaliyahToContent(db: SupportSQLiteDatabase) {
            val amaliyahCursor = db.query("SELECT id, slug, titleId, descriptionId, category FROM amaliyah")
            amaliyahCursor.use { amaliyah ->
                while (amaliyah.moveToNext()) {
                    val amaliyahId = amaliyah.getString(0)
                    val slug = amaliyah.getString(1)
                    val titleId = amaliyah.getString(2)
                    val descriptionId = if (amaliyah.isNull(3)) "" else amaliyah.getString(3)
                    val category = if (amaliyah.isNull(4)) null else amaliyah.getString(4)
                    val published = resolvePublishedVersion(db, amaliyahId) ?: continue

                    db.execSQL(
                        "INSERT INTO content (id, title, description, imageUrl, category, version, `order`, " +
                            "isActive, sourceName, sourceUrl) VALUES (?, ?, ?, NULL, ?, ?, 0, 1, ?, ?)",
                        arrayOf<Any?>(
                            slug,
                            titleId,
                            descriptionId,
                            category,
                            published.versionNumber,
                            published.sourceName,
                            published.sourceReference,
                        ),
                    )

                    migrateSteps(db, published.versionId, slug)
                }
            }
        }

        /** The default variant's latest published version for one amaliyah, or `null` if either
         * lookup comes up empty (no default variant, or no published version for it). */
        private fun resolvePublishedVersion(
            db: SupportSQLiteDatabase,
            amaliyahId: String,
        ): PublishedVersion? {
            val variantId =
                db
                    .query(
                        SimpleSQLiteQuery(
                            "SELECT id FROM amaliyah_variants WHERE amaliyahId = ? AND isDefault = 1 LIMIT 1",
                            arrayOf(amaliyahId),
                        ),
                    ).use { if (it.moveToFirst()) it.getString(0) else null } ?: return null

            return db
                .query(
                    SimpleSQLiteQuery(
                        "SELECT id, versionNumber, sourceName, sourceReference FROM amaliyah_versions " +
                            "WHERE variantId = ? AND status = 'PUBLISHED' ORDER BY versionNumber DESC LIMIT 1",
                        arrayOf(variantId),
                    ),
                ).use {
                    if (it.moveToFirst()) {
                        PublishedVersion(it.getString(0), it.getInt(1), it.getString(2), it.getString(3))
                    } else {
                        null
                    }
                }
        }

        private fun migrateSteps(
            db: SupportSQLiteDatabase,
            versionId: String,
            contentId: String,
        ) {
            val stepCursor =
                db.query(
                    SimpleSQLiteQuery(
                        "SELECT id, stepType, arabicText, translationId, repeatTarget FROM amaliyah_steps " +
                            "WHERE versionId = ? ORDER BY position ASC",
                        arrayOf(versionId),
                    ),
                )
            var newPosition = 0
            stepCursor.use {
                while (it.moveToNext()) {
                    val stepType = it.getString(1)
                    val arabicText = if (it.isNull(2)) "" else it.getString(2)
                    val translation = if (it.isNull(3)) "" else it.getString(3)
                    val repeatTarget = if (it.isNull(4)) 1 else it.getInt(4).coerceAtLeast(1)
                    // Heading-only steps have real title text but no Arabic body (product-owner
                    // decision: drop rather than fabricate Arabic text for them). The blank-text
                    // check is a safety net: never carry forward a step the new schema would
                    // consider invalid.
                    if (stepType == "HEADING" || arabicText.isBlank() || translation.isBlank()) continue
                    newPosition += 1
                    db.execSQL(
                        "INSERT INTO content_steps (id, contentId, position, arabicText, translation, repeatTarget) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf<Any?>(it.getString(0), contentId, newPosition, arabicText, translation, repeatTarget),
                    )
                }
            }
        }

        private fun remapReadingPositions(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `reading_positions_new` (`contentId` TEXT NOT NULL, " +
                    "`itemIndex` INTEGER NOT NULL, `itemOffset` INTEGER NOT NULL, " +
                    "`lastOpenedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`contentId`))",
            )
            db.execSQL(
                """
                INSERT INTO reading_positions_new (contentId, itemIndex, itemOffset, lastOpenedAtEpochMillis)
                SELECT am.slug, rp.itemIndex, rp.itemOffset, rp.lastOpenedAtEpochMillis
                FROM reading_positions rp
                JOIN amaliyah_versions av ON av.id = rp.versionId
                JOIN amaliyah_variants avar ON avar.id = av.variantId
                JOIN amaliyah am ON am.id = avar.amaliyahId
                WHERE am.slug IN (SELECT id FROM content)
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE reading_positions")
            db.execSQL("ALTER TABLE reading_positions_new RENAME TO reading_positions")
        }

        private fun remapGuidedReadingSessions(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `guided_reading_sessions_new` (`contentId` TEXT NOT NULL, " +
                    "`currentStepId` TEXT NOT NULL, `lastOpenedAtEpochMillis` INTEGER NOT NULL, " +
                    "`completedAtEpochMillis` INTEGER, `startedAtEpochMillis` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`contentId`))",
            )
            db.execSQL(
                """
                INSERT INTO guided_reading_sessions_new
                    (contentId, currentStepId, lastOpenedAtEpochMillis, completedAtEpochMillis, startedAtEpochMillis)
                SELECT am.slug, grs.currentStepId, grs.lastOpenedAtEpochMillis, grs.completedAtEpochMillis,
                       grs.startedAtEpochMillis
                FROM guided_reading_sessions grs
                JOIN amaliyah_versions av ON av.id = grs.versionId
                JOIN amaliyah_variants avar ON avar.id = av.variantId
                JOIN amaliyah am ON am.id = avar.amaliyahId
                WHERE am.slug IN (SELECT id FROM content)
                  AND grs.currentStepId IN (SELECT id FROM content_steps)
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE guided_reading_sessions")
            db.execSQL("ALTER TABLE guided_reading_sessions_new RENAME TO guided_reading_sessions")
        }

        private fun remapStepProgress(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `step_progress_new` (`contentId` TEXT NOT NULL, " +
                    "`stepId` TEXT NOT NULL, `currentCount` INTEGER NOT NULL, " +
                    "`updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`contentId`, `stepId`))",
            )
            db.execSQL(
                """
                INSERT INTO step_progress_new (contentId, stepId, currentCount, updatedAtEpochMillis)
                SELECT am.slug, sp.stepId, sp.currentCount, sp.updatedAtEpochMillis
                FROM step_progress sp
                JOIN amaliyah_versions av ON av.id = sp.versionId
                JOIN amaliyah_variants avar ON avar.id = av.variantId
                JOIN amaliyah am ON am.id = avar.amaliyahId
                WHERE am.slug IN (SELECT id FROM content)
                  AND sp.stepId IN (SELECT id FROM content_steps)
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE step_progress")
            db.execSQL("ALTER TABLE step_progress_new RENAME TO step_progress")
        }

        private fun dropOldTables(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS amaliyah_steps")
            db.execSQL("DROP TABLE IF EXISTS amaliyah_versions")
            db.execSQL("DROP TABLE IF EXISTS approvals")
            db.execSQL("DROP TABLE IF EXISTS amaliyah_variants")
            db.execSQL("DROP TABLE IF EXISTS amaliyah")
        }
    }

/** The default variant's latest published version for one amaliyah, resolved by [MIGRATION_1_2]. */
private data class PublishedVersion(
    val versionId: String,
    val versionNumber: Int,
    val sourceName: String,
    val sourceReference: String,
)
