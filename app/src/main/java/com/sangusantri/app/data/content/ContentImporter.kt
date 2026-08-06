package com.sangusantri.app.data.content

import androidx.room.withTransaction
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.ContentEntity
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Canonical transactional Room operation for one catalog item (ADR 0015). Does not know whether
 * [importContentFile]'s bytes came from bundled assets
 * ([com.sangusantri.app.data.local.content.BundledContentBootstrapper]) or Firebase Hosting
 * ([com.sangusantri.app.data.sync.ContentSyncManager]) — both call this class.
 *
 * Two independent operations: [refreshCatalogMetadata] always updates the cheap, catalog-only
 * fields (title/description/imageUrl/category/order/isActive) for an item Room already has,
 * without fetching its content file — this is how `isActive=false`, reordering, and renaming
 * propagate without a network fetch. [importContentFile] is the more expensive, version-gated
 * operation that fetches, validates, and atomically replaces a content item's steps — and, for a
 * brand new item, creates its row for the first time (a new item is never visible in Room, and
 * therefore never visible on Beranda, until this succeeds).
 */
class ContentImporter
    @Inject
    constructor(
        private val database: SanguSantriDatabase,
    ) {
        private val contentDao get() = database.contentDao()
        private val contentStepDao get() = database.contentStepDao()
        private val readingPositionDao get() = database.readingPositionDao()
        private val guidedReadingSessionDao get() = database.guidedReadingSessionDao()
        private val stepProgressDao get() = database.stepProgressDao()

        /** Room's current version for a content id, used by callers to decide whether a content file
         * is even worth fetching before spending any bandwidth/IO on it. */
        suspend fun localVersion(contentId: String): Int? = contentDao.getById(contentId)?.version

        /** Cheap, no-fetch metadata refresh for an item Room already has. No-op for a brand new item
         * (its row cannot be created without the content file — see [importContentFile]). */
        suspend fun refreshCatalogMetadata(item: ContentCatalogItemDto) {
            val existing = contentDao.getById(item.id) ?: return
            contentDao.upsert(
                existing.copy(
                    title = item.title,
                    description = item.description,
                    imageUrl = item.imageUrl,
                    category = item.category,
                    order = item.order,
                    isActive = item.isActive,
                ),
            )
        }

        @Suppress("ReturnCount")
        suspend fun importContentFile(
            item: ContentCatalogItemDto,
            file: ContentFileDto,
        ): ContentImportOutcome {
            if (file.id != item.id) {
                return ContentImportOutcome.Rejected(
                    item.id,
                    "content file id ${file.id} does not match catalog id ${item.id}",
                )
            }
            if (file.version != item.version) {
                return ContentImportOutcome.Rejected(
                    item.id,
                    "content file version ${file.version} does not match catalog version ${item.version}",
                )
            }
            val validation = ContentValidator.validateContentFile(file)
            if (validation is ContentValidation.Invalid) {
                return ContentImportOutcome.Rejected(item.id, validation.reason)
            }

            val existing = contentDao.getById(item.id)
            return when {
                existing == null ->
                    writeContentOrReject(item, file, previousStepIds = emptyList()) {
                        ContentImportOutcome.Imported(item.id)
                    }

                file.version < existing.version -> ContentImportOutcome.SkippedOlderVersion(item.id, existing.version)

                file.version == existing.version -> ContentImportOutcome.SkippedUpToDate(item.id)

                else -> {
                    val previousStepIds = contentStepDao.getByContentId(item.id).map { it.id }
                    writeContentOrReject(item, file, previousStepIds) {
                        ContentImportOutcome.Replaced(item.id, existing.version, file.version)
                    }
                }
            }
        }

        /**
         * [writeContent] runs inside a Room transaction that Room itself rolls back on any thrown
         * exception (e.g. a primary-key conflict) — but the exception still propagates out of the
         * transaction lambda. Catching it here, not in each caller
         * ([com.sangusantri.app.data.local.content.BundledContentBootstrapper] and
         * [com.sangusantri.app.data.sync.ContentSyncManager]), is what makes
         * [ContentImportOutcome.Rejected]'s own contract ("a database failure that rolled back")
         * true regardless of which caller triggered the write — this is also the difference between
         * one item's database failure aborting only that item versus taking down a whole sync/
         * bootstrap pass (PRD 12.4, per-item failure isolation).
         */
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        private suspend fun writeContentOrReject(
            item: ContentCatalogItemDto,
            file: ContentFileDto,
            previousStepIds: List<String>,
            onSuccess: () -> ContentImportOutcome,
        ): ContentImportOutcome =
            try {
                writeContent(item, file, previousStepIds)
                onSuccess()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (databaseFailure: Exception) {
                ContentImportOutcome.Rejected(item.id, "database failure during import: ${databaseFailure.message}")
            }

        private suspend fun writeContent(
            item: ContentCatalogItemDto,
            file: ContentFileDto,
            previousStepIds: List<String>,
        ) {
            database.withTransaction {
                contentDao.upsert(
                    ContentEntity(
                        id = item.id,
                        title = item.title,
                        description = item.description,
                        imageUrl = item.imageUrl,
                        category = item.category,
                        version = file.version,
                        order = item.order,
                        isActive = item.isActive,
                        sourceName = file.sourceName,
                        sourceUrl = file.sourceUrl,
                    ),
                )
                if (previousStepIds.isNotEmpty()) {
                    contentStepDao.deleteByContentId(item.id)
                }
                contentStepDao.insertAll(
                    file.steps.mapIndexed { index, step -> step.toEntity(contentId = item.id, position = index + 1) },
                )

                // Progress migration only matters when replacing an existing item's steps, not on a
                // fresh import (there is no prior progress to reason about yet).
                if (previousStepIds.isNotEmpty()) {
                    val survivingStepIds = file.steps.map { it.id }
                    stepProgressDao.deleteOrphaned(item.id, survivingStepIds)
                    guidedReadingSessionDao.deleteIfCurrentStepMissing(item.id, survivingStepIds)
                    // Full Reader's index-based scroll position cannot be meaningfully preserved once
                    // the step list itself changes — indices may now point at different content.
                    readingPositionDao.deleteByContentId(item.id)
                }
            }
        }
    }
