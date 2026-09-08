package com.sangusantri.app.data.content

import androidx.room.withTransaction
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
import com.sangusantri.app.domain.model.toContentLayout
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Canonical transactional Room operation for one CMS content item (`schemaVersion` 3), used by
 * [com.sangusantri.app.data.sync.ContentSyncManager] and
 * [com.sangusantri.app.data.sync.ContentDetailSyncManager].
 *
 * Two entry points: [importListItem] takes a metadata listing that makes an item visible, and
 * [importRemoteDetail] takes a detail that brings its steps and decides against Room whether
 * anything actually changed.
 *
 * Both converge on the same guarantee: a content item's steps are replaced atomically or not at
 * all, and a brand new item is not visible in Room — and therefore not on Beranda — until its
 * write succeeds.
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

    /**
     * Imports one entry from a CMS list response (`schemaVersion` 3) — display metadata only.
     *
     * A brand-new item gets a row here *without steps*, which is a deliberate change from the
     * old contract: the list is what Beranda draws from, and an item has to be visible before
     * the reader can tap it to fetch its detail. Until that happens the row has no steps and
     * empty source attribution; [importRemoteDetail] fills both in.
     *
     * Existing rows keep their steps, their local revision counter, their progress, and their
     * layout — nothing here touches content, only how the card looks and whether it is shown.
     * `layout` in particular is *not* on the list contract, so writing it here would overwrite a
     * known-good value with a default on every Beranda resume.
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

        val existing =
            contentDao.getById(detail.id)
                ?: return writeDetailOrReject(detail, version = FIRST_VERSION, replacesSteps = false) {
                    ContentImportOutcome.Imported(detail.id)
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
                // Flipping an item's layout edits no step, so this — the steps-unchanged path —
                // is exactly the one a layout correction arrives on. Left out here it would
                // never land.
                layout = detail.layout.toContentLayout(),
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
                    layout = detail.layout.toContentLayout(),
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

    private companion object {
        /** A brand-new item starts at revision 1; nothing remote supplies this any more. */
        const val FIRST_VERSION = 1
    }
}
