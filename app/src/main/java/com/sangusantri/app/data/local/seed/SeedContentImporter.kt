package com.sangusantri.app.data.local.seed

import androidx.room.withTransaction
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.seed.dto.ContentManifestDto
import com.sangusantri.app.data.local.seed.dto.ContentManifestEntryDto
import com.sangusantri.app.data.local.seed.dto.ContentPackageDto
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Idempotent, transactional import of bundled seed content (PRD 12.2, FR-001).
 * Each manifest package is read, checksum-verified, structurally validated, and
 * imported independently: a malformed or already-imported package never affects
 * another (PRD 12.4), and every write for one package happens inside a single
 * Room transaction that rolls back completely on any failure.
 */
class SeedContentImporter
    @Inject
    constructor(
        private val seedContentSource: SeedContentSource,
        private val database: SanguSantriDatabase,
    ) {
        private val json = Json { ignoreUnknownKeys = true }
        private val amaliyahDao get() = database.amaliyahDao()
        private val amaliyahVariantDao get() = database.amaliyahVariantDao()
        private val approvalDao get() = database.approvalDao()
        private val amaliyahVersionDao get() = database.amaliyahVersionDao()
        private val amaliyahStepDao get() = database.amaliyahStepDao()

        suspend fun importSeedContent(): List<SeedImportOutcome> {
            val manifest = readManifest()
            return when {
                manifest == null -> listOf(SeedImportOutcome.Failed(null, MANIFEST_READ_ERROR))
                manifest.schemaVersion != SeedContentValidator.SUPPORTED_SCHEMA_VERSION -> {
                    val reason = "unsupported manifest schemaVersion ${manifest.schemaVersion}"
                    listOf(SeedImportOutcome.Failed(null, reason))
                }
                else -> manifest.packages.map { entry -> importPackage(entry) }
            }
        }

        private fun readManifest(): ContentManifestDto? =
            runCatching {
                json.decodeFromString<ContentManifestDto>(seedContentSource.readManifest().decodeToString())
            }.getOrNull()

        private suspend fun importPackage(entry: ContentManifestEntryDto): SeedImportOutcome {
            if (amaliyahVersionDao.existsById(entry.versionId)) {
                return SeedImportOutcome.AlreadyImported(entry.versionId)
            }

            val result =
                runCatching {
                    val bytes = seedContentSource.readPackage(entry.file)
                    val actualChecksum = SeedContentChecksum.sha256Hex(bytes)
                    check(actualChecksum.equals(entry.checksumSha256, ignoreCase = true)) { "checksum mismatch" }

                    val pkg = json.decodeFromString<ContentPackageDto>(bytes.decodeToString())
                    val validation = SeedContentValidator.validate(pkg)
                    check(validation is ContentPackageValidation.Valid) {
                        (validation as ContentPackageValidation.Invalid).reason
                    }

                    importValidatedPackage(pkg, actualChecksum)
                }

            return result.fold(
                onSuccess = { SeedImportOutcome.Imported(entry.versionId) },
                onFailure = { SeedImportOutcome.Failed(entry.versionId, it.message ?: "unknown import error") },
            )
        }

        private suspend fun importValidatedPackage(
            pkg: ContentPackageDto,
            checksumSha256: String,
        ) {
            database.withTransaction {
                if (!amaliyahDao.existsById(pkg.amaliyah.id)) {
                    amaliyahDao.insert(pkg.amaliyah.toEntity())
                }
                if (!amaliyahVariantDao.existsById(pkg.variant.id)) {
                    amaliyahVariantDao.insert(pkg.variant.toEntity(pkg.amaliyah.id))
                }
                if (!approvalDao.existsById(pkg.approval.id)) {
                    approvalDao.insert(pkg.approval.toEntity())
                }
                amaliyahVersionDao.insert(
                    pkg.version.toEntity(
                        variantId = pkg.variant.id,
                        schemaVersion = pkg.schemaVersion,
                        approvalId = pkg.approval.id,
                        checksumSha256 = checksumSha256,
                    ),
                )
                amaliyahStepDao.insertAll(pkg.steps.map { it.toEntity(pkg.version.id) })
            }
        }

        private companion object {
            const val MANIFEST_READ_ERROR = "unable to read or parse manifest.json"
        }
    }
