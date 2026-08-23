package com.sangusantri.app.data.content

import androidx.room.withTransaction
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Canonical transactional Room operation for one catalog item (ADR 0015). Does not know whether
 * [importContentFile]'s bytes came from bundled assets
 * ([com.sangusantri.app.data.local.content.BundledContentBootstrapper]) or the CMS API
 * ([com.sangusantri.app.data.sync.ContentSyncManager]) — both call this class.
 *
 * Three entry points across two contracts. [importListItem] and [importRemoteDetail] are the CMS
 * API path (`schemaVersion` 3): a metadata listing that makes an item visible, and a detail that
 * brings its steps and decides against Room whether anything actually changed.
 * [importContentFile] plus [refreshCatalogMetadata] are the bundled-asset path (`schemaVersion` 1,
 * catalog plus package files, version-gated) and are unchanged.
 *
 * Both converge on the same guarantee: a content item's steps are replaced atomically or not at
 * all, and a brand new item is not visible in Room — and therefore not on Beranda — until its
 * write succeeds.
 *
 * Two import contracts, each needing its own validate/compare/write trio, is what puts this class
 * over detekt's function threshold — splitting them apart would duplicate the transactional write
 * that is the entire point of having one importer.
 */
@Suppress("TooManyFunctions")
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
         * is even worth fetching before spending any bandwidth/IO on it. Bundled bootstrap only —
         * the CMS stopped publishing versions (see [importRemoteDetail]). */
        suspend fun localVersion(contentId: String): Int? = contentDao.getById(contentId)?.version

    /**
     * Imports one entry from a CMS list response (`schemaVersion` 3) — display metadata only.
     *
     * A brand-new item gets a row here *without steps*, which is a deliberate change from the
     * old contract: the list is what Beranda draws from, and an item has to be visible before
     * the reader can tap it to fetch its detail. Until that happens the row has no steps and
     * empty source attribution; [importRemoteDetail] fills both in.
     *
     * Existing rows keep their steps, their local revision counter, and their progress —
     * nothing here touches content, only how the card looks and whether it is shown.
     */
    suspend fun importListItem(item: ContentListItemDto): ContentImportOutcome {
        val validation = ContentValidator.validateListItem(item)
        if (validation is ContentValidation.Invalid) {
            return ContentImportOutcome.Rejected(item.id, validation.reason)
        }

        val existing = contentDao.getById(item.id)
        return runCatching {
            contentDao.upsert(
                existing?.copy(
                    title = item.title,
                    description = item.description,
                    imageUrl = item.imageUrl,
                    category = item.category,
                    order = item.order,
                    isActive = true,
                ) ?: ContentEntity(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    imageUrl = item.imageUrl,
                    category = item.category,
                    version = FIRST_VERSION,
                    order = item.order,
                    isActive = true,
                    // Filled by the first successful detail fetch. Kept as "" rather than made
                    // nullable on purpose: changing the column's nullability changes the Room
                    // schema, and this app's standing fallbackToDestructiveMigration would
                    // then wipe every table on upgrade.
                    sourceName = "",
                    sourceUrl = "",
                ),
            )
            if (existing == null) {
                ContentImportOutcome.Imported(item.id)
            } else {
                ContentImportOutcome.SkippedUpToDate(item.id)
            }
        }.getOrElse { failure ->
            ContentImportOutcome.Rejected(item.id, "database failure during list import: ${failure.message}")
        }
    }

    /**
     * Imports one item's detail (`schemaVersion` 3), fetched when the reader opens it.
     *
     * **What changed is decided here, against Room, not by the server.** The old contract sent a
     * per-item `version` and the client re-imported when it increased. Now the detail is fetched
     * on open and its ETag already spares the download when nothing moved — but a `200` still
     * has to be checked, because rewriting steps that did not change would throw away the
     * reader's position and guided-session state in content nobody edited.
     *
     * [ContentEntity.version] survives as a purely local revision counter, incremented once per
     * genuine content replacement. Nothing sends it, but `amaliyah_completion_events.versionNumber`
     * records it against every completion, and that history is meant to outlive the content.
     */
    @Suppress("ReturnCount")
    suspend fun importRemoteDetail(detail: ContentDetailDto): ContentImportOutcome {
        val validation = ContentValidator.validateDetail(detail)
        if (validation is ContentValidation.Invalid) {
            return ContentImportOutcome.Rejected(detail.id, validation.reason)
        }

        val existing = contentDao.getById(detail.id)
        if (existing == null) {
            return writeDetailOrReject(detail, version = FIRST_VERSION, replacesSteps = false) {
                ContentImportOutcome.Imported(detail.id)
            }
        }

        val storedSteps = contentStepDao.getByContentId(detail.id)
        // A row created from the list has no steps yet, so "unchanged" must not be read off two
        // empty lists — that would leave the reader permanently empty.
        if (storedSteps.isNotEmpty() && stepsUnchanged(storedSteps, detail.steps)) {
            refreshDetailMetadata(detail, existing)
            return ContentImportOutcome.SkippedUpToDate(detail.id)
        }

        val nextVersion = if (storedSteps.isEmpty()) existing.version else existing.version + 1
        return writeDetailOrReject(detail, nextVersion, replacesSteps = storedSteps.isNotEmpty()) {
            ContentImportOutcome.Replaced(detail.id, existing.version, nextVersion)
        }
    }

    /**
     * Hides items the CMS no longer publishes, returning their count.
     *
     * Only ever called when *both* list requests succeeded — a failed request means "no
     * information", not "nothing is published", and treating the two alike would blank Beranda
     * on a bad network. An entirely empty published set is refused for the same reason: it is
     * far more likely a CMS mistake than an intentional unpublish-everything, and this app's
     * whole posture is that keeping content is the safe default.
     */
    suspend fun deactivateAbsent(publishedIds: List<String>): Int =
        if (publishedIds.isEmpty()) 0 else contentDao.deactivateAbsent(publishedIds)

    /**
     * Step-list equality as the reader would experience it: same steps, same words, same
     * counters, same order. Deliberately ignores anything not on the wire — Room's `position`
     * is derived from array index, so comparing the ordered lists pairwise already covers it.
     */
    private fun stepsUnchanged(
        stored: List<ContentStepEntity>,
        incoming: List<ContentStepDto>,
    ): Boolean =
        stored.size == incoming.size &&
            stored.zip(incoming).all { (storedStep, incomingStep) ->
                storedStep.id == incomingStep.id &&
                    storedStep.arabicText == incomingStep.arabicText &&
                    storedStep.translation == incomingStep.translation &&
                    storedStep.repeatTarget == incomingStep.repeatTarget
            }

    /** The no-rewrite path for a detail whose steps are identical: refresh the fields the
     * detail owns and leave steps, revision and progress alone. */
    private suspend fun refreshDetailMetadata(
        detail: ContentDetailDto,
        existing: ContentEntity,
    ) {
        contentDao.upsert(
            existing.copy(
                title = detail.title,
                description = detail.description,
                imageUrl = detail.imageUrl,
                category = detail.category,
                order = detail.order,
                isActive = true,
                sourceName = detail.sourceName,
                sourceUrl = detail.sourceUrl,
            ),
        )
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun writeDetailOrReject(
        detail: ContentDetailDto,
        version: Int,
        replacesSteps: Boolean,
        onSuccess: () -> ContentImportOutcome,
    ): ContentImportOutcome =
        try {
            writeDetail(detail, version, replacesSteps)
            onSuccess()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (databaseFailure: Exception) {
            ContentImportOutcome.Rejected(detail.id, "database failure during import: ${databaseFailure.message}")
        }

    private suspend fun writeDetail(
        detail: ContentDetailDto,
        version: Int,
        replacesSteps: Boolean,
    ) {
        database.withTransaction {
            contentDao.upsert(
                ContentEntity(
                    id = detail.id,
                    title = detail.title,
                    description = detail.description,
                    imageUrl = detail.imageUrl,
                    category = detail.category,
                    version = version,
                    order = detail.order,
                    // Being served at all is what "published" means now; there is no isActive
                    // field on the wire, and an unpublished item is simply a 404 here.
                    isActive = true,
                    sourceName = detail.sourceName,
                    sourceUrl = detail.sourceUrl,
                ),
            )
            if (replacesSteps) {
                contentStepDao.deleteByContentId(detail.id)
            }
            contentStepDao.insertAll(
                detail.steps.mapIndexed { index, step ->
                    step.toEntity(contentId = detail.id, position = index + 1)
                },
            )

            if (replacesSteps) {
                val survivingStepIds = detail.steps.map { it.id }
                stepProgressDao.deleteOrphaned(detail.id, survivingStepIds)
                guidedReadingSessionDao.deleteIfCurrentStepMissing(detail.id, survivingStepIds)
                // Full Reader's index-based scroll position cannot be meaningfully preserved once
                // the step list itself changes — indices may now point at different content.
                readingPositionDao.deleteByContentId(detail.id)
            }
        }
    }

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

    private companion object {
        /** A brand-new item starts at revision 1; nothing remote supplies this any more. */
        const val FIRST_VERSION = 1
    }
    }
