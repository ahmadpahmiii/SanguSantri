package com.sangusantri.app.data.sync

import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentPackageImporter
import com.sangusantri.app.data.content.ContentPackageValidator
import com.sangusantri.app.data.remote.ContentRemoteDataSource
import com.sangusantri.app.data.remote.ManifestFetchOutcome
import com.sangusantri.app.data.remote.PackageFetchOutcome
import com.sangusantri.app.data.remote.RemoteContentFailure
import com.sangusantri.app.data.remote.dto.RemoteContentManifestPackageDto
import javax.inject.Inject

/**
 * Remote content sync algorithm (section 12). Per package: skip without downloading when the
 * manifest-declared version/checksum already matches what [ContentPackageImporter] has active
 * (bandwidth avoidance), otherwise download and delegate to the same transactional importer the
 * bundled bootstrapper uses. One malformed or stale package never affects another.
 */
class ContentSyncCoordinator
@Inject
constructor(
    private val remoteDataSource: ContentRemoteDataSource,
    private val contentPackageImporter: ContentPackageImporter,
    private val syncMetadata: ContentSyncMetadata,
) {
    suspend fun sync(): ContentSyncOutcome =
        when (val manifestOutcome = remoteDataSource.fetchManifest(syncMetadata.getStoredEtag())) {
            is ManifestFetchOutcome.NotModified -> ContentSyncOutcome.NotModified
            is ManifestFetchOutcome.Failed -> ContentSyncOutcome.Failed(manifestOutcome.failure)
            is ManifestFetchOutcome.Fetched -> processManifest(manifestOutcome)
        }

    private suspend fun processManifest(fetched: ManifestFetchOutcome.Fetched): ContentSyncOutcome {
        if (fetched.manifest.schemaVersion != ContentPackageValidator.SUPPORTED_SCHEMA_VERSION) {
            return ContentSyncOutcome.Failed(
                RemoteContentFailure.MalformedBody(
                    "unsupported manifest schemaVersion ${fetched.manifest.schemaVersion}",
                ),
            )
        }

        val info = ManifestSyncInfo(etag = fetched.etag, manifestVersion = fetched.manifest.manifestVersion)
        val results = fetched.manifest.packages.map { entry -> syncPackage(entry) }
        return classify(info, results)
    }

    private fun classify(
        info: ManifestSyncInfo,
        results: List<ContentImportOutcome>,
    ): ContentSyncOutcome {
        val updated =
            results.filterIsInstance<ContentImportOutcome.Imported>().map { it.versionId } +
                results.filterIsInstance<ContentImportOutcome.Replaced>().map { it.newVersionId }
        val failed =
            results.filterIsInstance<ContentImportOutcome.Rejected>().mapNotNull { it.versionId } +
                results.filterIsInstance<ContentImportOutcome.ChecksumConflict>().map { it.versionId }

        return when {
            updated.isEmpty() && failed.isEmpty() -> ContentSyncOutcome.NoChanges(info)
            failed.isEmpty() -> ContentSyncOutcome.Updated(info, updated)
            updated.isEmpty() -> ContentSyncOutcome.CompleteFailure(info, failed)
            else -> ContentSyncOutcome.PartialFailure(info, updated, failed)
        }
    }

    private suspend fun syncPackage(entry: RemoteContentManifestPackageDto): ContentImportOutcome {
        if (entry.minimumAppVersionCode > BuildConfig.VERSION_CODE) {
            return ContentImportOutcome.Rejected(
                entry.versionId,
                "requires app versionCode ${entry.minimumAppVersionCode}, current is ${BuildConfig.VERSION_CODE}",
            )
        }

        val active = contentPackageImporter.activeVersionSummary(entry.variantId)
        return when {
            active == null -> downloadAndImport(entry)
            entry.versionNumber < active.versionNumber ->
                ContentImportOutcome.SkippedOlderVersion(entry.versionId, active.versionId)

            entry.versionNumber == active.versionNumber ->
                if (entry.checksumSha256.equals(active.checksumSha256, ignoreCase = true)) {
                    ContentImportOutcome.AlreadyUpToDate(entry.versionId)
                } else {
                    ContentImportOutcome.ChecksumConflict(entry.versionId)
                }

            else -> downloadAndImport(entry)
        }
    }

    private suspend fun downloadAndImport(entry: RemoteContentManifestPackageDto): ContentImportOutcome =
        when (val fetch = remoteDataSource.fetchPackage(entry.versionId)) {
            is PackageFetchOutcome.Failed -> ContentImportOutcome.Rejected(entry.versionId, describe(fetch.failure))
            is PackageFetchOutcome.Fetched ->
                contentPackageImporter.importPackage(fetch.bytes, entry.versionId, entry.checksumSha256)
        }

    private fun describe(failure: RemoteContentFailure): String =
        when (failure) {
            RemoteContentFailure.NoConnectivityOrTimeout -> "network error"
            is RemoteContentFailure.HttpStatus -> "HTTP ${failure.code}"
            is RemoteContentFailure.PayloadTooLarge -> "package exceeds ${failure.limitBytes} bytes"
            is RemoteContentFailure.MalformedBody -> failure.reason
        }
}
