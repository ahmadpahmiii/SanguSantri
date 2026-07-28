package com.sangusantri.app.data.content

import androidx.room.withTransaction
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.content.dto.ContentPackageDto
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Canonical transactional Room operation for a content package (PRD 12.2, 12.4). Does not know
 * whether [importPackage]'s bytes came from bundled assets
 * ([com.sangusantri.app.data.local.content.BundledContentBootstrapper]) or the backend
 * ([com.sangusantri.app.data.sync.ContentSyncCoordinator]) — both call this class.
 *
 * Android keeps only the current active version per variant (no previous-version retention or
 * fallback): a package with a higher `versionNumber` than Room's active version replaces it
 * atomically, including its version-scoped reading progress; an older or checksum-conflicting
 * package is rejected without writing anything.
 */
class ContentPackageImporter
@Inject
constructor(
    private val database: SanguSantriDatabase,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val amaliyahDao get() = database.amaliyahDao()
    private val amaliyahVariantDao get() = database.amaliyahVariantDao()
    private val approvalDao get() = database.approvalDao()
    private val amaliyahVersionDao get() = database.amaliyahVersionDao()
    private val amaliyahStepDao get() = database.amaliyahStepDao()
    private val readingPositionDao get() = database.readingPositionDao()
    private val guidedReadingSessionDao get() = database.guidedReadingSessionDao()
    private val stepProgressDao get() = database.stepProgressDao()

    /** Room's currently active version for a variant, used by callers to decide whether a
     * remote package is even worth downloading before spending any bandwidth on it. */
    suspend fun activeVersionSummary(variantId: String): ActiveVersionSummary? =
        amaliyahVersionDao.getActiveForVariant(variantId)?.let {
            ActiveVersionSummary(it.id, it.versionNumber, it.checksumSha256)
        }

    // Four sequential, independent checks that each short-circuit on rejection — flat guard
    // clauses are clearer here than folding them into nested expressions (matches
    // ContentPackageValidator.validateSteps's established convention in this codebase).
    @Suppress("ReturnCount")
    suspend fun importPackage(
        rawBytes: ByteArray,
        expectedVersionId: String,
        expectedChecksumSha256: String,
    ): ContentImportOutcome {
        val actualChecksum = ContentChecksum.sha256Hex(rawBytes)
        if (!actualChecksum.equals(expectedChecksumSha256, ignoreCase = true)) {
            return ContentImportOutcome.Rejected(expectedVersionId, "checksum mismatch")
        }

        val pkg =
            runCatching { json.decodeFromString<ContentPackageDto>(rawBytes.decodeToString()) }
                .getOrElse {
                    return ContentImportOutcome.Rejected(expectedVersionId, "malformed package JSON: ${it.message}")
                }

        validatePackage(pkg, expectedVersionId)?.let { rejection -> return rejection }

        return runCatching { applyAgainstRoom(pkg, actualChecksum) }
            .getOrElse { ContentImportOutcome.Rejected(pkg.version.id, it.message ?: "database write failed") }
    }

    @Suppress("ReturnCount")
    private fun validatePackage(
        pkg: ContentPackageDto,
        expectedVersionId: String,
    ): ContentImportOutcome.Rejected? {
        if (pkg.version.id != expectedVersionId) {
            return ContentImportOutcome.Rejected(
                expectedVersionId,
                "package version id ${pkg.version.id} does not match declared $expectedVersionId",
            )
        }
        val validation = ContentPackageValidator.validate(pkg)
        if (validation is ContentPackageValidation.Invalid) {
            return ContentImportOutcome.Rejected(pkg.version.id, validation.reason)
        }
        if (pkg.version.minimumAppVersionCode > BuildConfig.VERSION_CODE) {
            val required = pkg.version.minimumAppVersionCode
            val current = BuildConfig.VERSION_CODE
            return ContentImportOutcome.Rejected(
                pkg.version.id,
                "requires app versionCode $required, current is $current",
            )
        }
        return null
    }

    private suspend fun applyAgainstRoom(
        pkg: ContentPackageDto,
        checksumSha256: String,
    ): ContentImportOutcome {
        val active = amaliyahVersionDao.getActiveForVariant(pkg.variant.id)
        return when {
            active == null -> {
                writePackage(pkg, checksumSha256, oldActive = null)
                ContentImportOutcome.Imported(pkg.version.id)
            }

            pkg.version.versionNumber < active.versionNumber ->
                ContentImportOutcome.SkippedOlderVersion(pkg.version.id, active.id)

            pkg.version.versionNumber == active.versionNumber ->
                if (checksumSha256.equals(active.checksumSha256, ignoreCase = true)) {
                    ContentImportOutcome.AlreadyUpToDate(pkg.version.id)
                } else {
                    ContentImportOutcome.ChecksumConflict(pkg.version.id)
                }

            else -> {
                writePackage(pkg, checksumSha256, oldActive = active)
                ContentImportOutcome.Replaced(active.id, pkg.version.id)
            }
        }
    }

    // Insert the new package first, then remove the old one: both immutable ids differ, so no
    // unique-index conflict is possible between the two steps (section 14 ordering).
    private suspend fun writePackage(
        pkg: ContentPackageDto,
        checksumSha256: String,
        oldActive: AmaliyahVersionEntity?,
    ) {
        database.withTransaction {
            amaliyahDao.upsert(pkg.amaliyah.toEntity())
            amaliyahVariantDao.upsert(pkg.variant.toEntity(pkg.amaliyah.id))
            approvalDao.insert(pkg.approval.toEntity())
            amaliyahVersionDao.insert(
                pkg.version.toEntity(
                    variantId = pkg.variant.id,
                    schemaVersion = pkg.schemaVersion,
                    approvalId = pkg.approval.id,
                    checksumSha256 = checksumSha256,
                ),
            )
            amaliyahStepDao.insertAll(pkg.steps.map { it.toEntity(pkg.version.id) })

            if (oldActive != null) {
                readingPositionDao.deleteByVersionId(oldActive.id)
                guidedReadingSessionDao.deleteByVersionId(oldActive.id)
                stepProgressDao.deleteByVersionId(oldActive.id)
                amaliyahVersionDao.deleteById(oldActive.id)
                approvalDao.deleteById(oldActive.approvalId)
            }
        }
    }

    data class ActiveVersionSummary(
        val versionId: String,
        val versionNumber: Int,
        val checksumSha256: String,
    )
}
