package com.sangusantri.app.data.local.content

import android.content.Context
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentPackageImporter
import com.sangusantri.app.data.content.ContentPackageValidator
import com.sangusantri.app.data.content.ContentVersionAction
import com.sangusantri.app.data.content.decideContentVersionAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Reads bundled content straight from [android.content.res.AssetManager] (no source-abstraction
 * interface — this is the only bundled-storage implementation there is) and imports it through
 * the shared [ContentPackageImporter]. Guarantees offline content on a fresh install, recovers
 * content if the backend has never been reached, and reconciles the bundled baseline against
 * whatever remote sync has already put in Room without ever downgrading it (PRD 3.2, FR-001).
 *
 * Idempotent and safe to call on every launch: each entry is compared against Room's active
 * version for that variant *before* its package asset is even read (section 5) — an older or
 * already-current bundled entry never touches [android.content.res.AssetManager] at all, and a
 * genuinely newer bundled entry still goes through [ContentPackageImporter]'s own authoritative
 * comparison and checksum verification.
 */
class BundledContentBootstrapper
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val contentPackageImporter: ContentPackageImporter,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun bootstrap(): List<ContentImportOutcome> =
        withContext(Dispatchers.IO) {
            val manifest = readManifest()
            when {
                manifest == null ->
                    listOf(ContentImportOutcome.Rejected(null, "unable to read or parse bundled manifest.json"))

                manifest.schemaVersion != ContentPackageValidator.SUPPORTED_SCHEMA_VERSION ->
                    listOf(
                        ContentImportOutcome.Rejected(
                            null,
                            "unsupported bundled manifest schemaVersion ${manifest.schemaVersion}",
                        ),
                    )

                else -> manifest.packages.map { entry -> evaluate(entry) }
            }
        }

    private suspend fun evaluate(entry: BundledManifestEntryDto): ContentImportOutcome {
        val active = contentPackageImporter.activeVersionSummary(entry.variantId) ?: return readAndImport(entry)
        return when (decideContentVersionAction(entry.versionNumber, entry.checksumSha256, active)) {
            ContentVersionAction.SKIP_OLDER ->
                ContentImportOutcome.SkippedOlderVersion(entry.versionId, active.versionId)

            ContentVersionAction.SKIP_UP_TO_DATE -> ContentImportOutcome.AlreadyUpToDate(entry.versionId)
            ContentVersionAction.REJECT_CHECKSUM_CONFLICT -> ContentImportOutcome.ChecksumConflict(entry.versionId)
            ContentVersionAction.IMPORT -> readAndImport(entry)
            }
        }

    private suspend fun readAndImport(entry: BundledManifestEntryDto): ContentImportOutcome =
        runCatching {
            contentPackageImporter.importPackage(
                rawBytes = readAsset(entry.file),
                expectedVersionId = entry.versionId,
                expectedChecksumSha256 = entry.checksumSha256,
            )
        }.getOrElse {
            ContentImportOutcome.Rejected(
                entry.versionId,
                "unable to read bundled asset ${entry.file}: ${it.message}",
            )
        }

    private fun readManifest(): BundledManifestDto? =
        runCatching {
            json.decodeFromString<BundledManifestDto>(readAsset(MANIFEST_FILE_NAME).decodeToString())
        }.getOrNull()

    private fun readAsset(fileName: String): ByteArray =
        context.assets.open("$CONTENT_ASSET_DIR/$fileName").use { it.readBytes() }

    private companion object {
        const val CONTENT_ASSET_DIR = "content"
        const val MANIFEST_FILE_NAME = "manifest.json"
    }
}
