package com.sangusantri.app.data.sync

import android.content.Context
import android.util.Log
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentPackageImporter
import com.sangusantri.app.data.content.ContentPackageValidator
import com.sangusantri.app.data.content.ContentVersionAction
import com.sangusantri.app.data.content.decideContentVersionAction
import com.sangusantri.app.data.remote.api.ContentApiService
import com.sangusantri.app.data.remote.dto.RemoteContentManifestDto
import com.sangusantri.app.data.remote.dto.RemoteContentManifestPackageDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * One complete remote content-sync execution (section 8): fetch the manifest, compare every entry
 * against Room's active versions, download and import only genuinely newer/changed packages, and
 * return one [SyncResult]. All package writes still go through [ContentPackageImporter] — this
 * class never touches a content table directly. Retrofit [Response]/[ResponseBody]/HTTP status
 * codes/[IOException] are used here (this is the data/sync layer) but never escape as a return
 * type — callers only ever see [SyncResult].
 */
class ContentSyncManager
@Inject
constructor(
    private val api: ContentApiService,
    private val contentPackageImporter: ContentPackageImporter,
    @param:ApplicationContext private val context: Context,
) {
    suspend fun sync(): SyncResult =
        withContext(Dispatchers.IO) {
            when (val outcome = fetchManifest()) {
                is ManifestOutcome.Failure -> outcome.result
                is ManifestOutcome.Success -> processPackages(outcome.manifest.packages)
            }
        }

    private suspend fun fetchManifest(): ManifestOutcome =
        try {
            toManifestOutcome(api.getManifest())
        } catch (io: IOException) {
            Log.w(TAG, "content manifest fetch failed", io)
            ManifestOutcome.Failure(SyncResult.RetryableFailure("manifest network error"))
        } catch (malformed: SerializationException) {
            Log.w(TAG, "content manifest fetch failed", malformed)
            ManifestOutcome.Failure(SyncResult.PermanentFailure("malformed manifest body"))
        }

    @Suppress("ReturnCount")
    private fun toManifestOutcome(response: Response<RemoteContentManifestDto>): ManifestOutcome {
        if (!response.isSuccessful) {
            return ManifestOutcome.Failure(classifyHttpFailure(response.code(), source = "manifest"))
        }

        val manifest =
            response.body() ?: return ManifestOutcome.Failure(SyncResult.PermanentFailure("empty manifest body"))
        if (manifest.schemaVersion != ContentPackageValidator.SUPPORTED_SCHEMA_VERSION) {
            return ManifestOutcome.Failure(
                SyncResult.PermanentFailure("unsupported manifest schemaVersion ${manifest.schemaVersion}"),
            )
        }
        return ManifestOutcome.Success(manifest)
    }

    private fun classifyHttpFailure(
        code: Int,
        source: String,
    ): SyncResult =
        if (isRetryableHttpStatus(code)) {
            SyncResult.RetryableFailure("$source HTTP $code")
        } else {
            SyncResult.PermanentFailure("$source HTTP $code")
        }

    private suspend fun processPackages(entries: List<RemoteContentManifestPackageDto>): SyncResult {
        val updated = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val rejected = mutableListOf<String>()

        for (entry in entries) {
            when (val result = processPackage(entry)) {
                is PackageResult.Updated -> updated += result.versionId
                is PackageResult.Skipped -> skipped += result.versionId
                is PackageResult.Rejected -> rejected += result.versionId
                // A retryable package failure aborts the whole sync (section 14) — packages
                // already updated above stay in Room and are simply skipped on the next attempt.
                is PackageResult.Abort -> return SyncResult.RetryableFailure(result.reason)
            }
        }

        return SyncResult.Completed(updated, skipped, rejected)
    }

    private suspend fun processPackage(entry: RemoteContentManifestPackageDto): PackageResult {
        if (entry.minimumAppVersionCode > BuildConfig.VERSION_CODE) {
            return PackageResult.Rejected(entry.versionId)
        }

        val active = contentPackageImporter.activeVersionSummary(entry.variantId)
        return when (decideContentVersionAction(entry.versionNumber, entry.checksumSha256, active)) {
            ContentVersionAction.SKIP_OLDER, ContentVersionAction.SKIP_UP_TO_DATE ->
                PackageResult.Skipped(entry.versionId)

            ContentVersionAction.REJECT_CHECKSUM_CONFLICT -> PackageResult.Rejected(entry.versionId)
            ContentVersionAction.IMPORT -> downloadAndImport(entry)
        }
    }

    private suspend fun downloadAndImport(entry: RemoteContentManifestPackageDto): PackageResult =
        when (val download = fetchPackage(entry.versionId)) {
            is PackageDownload.Abort -> PackageResult.Abort(download.reason)
            is PackageDownload.Rejected -> PackageResult.Rejected(entry.versionId)
            is PackageDownload.Fetched ->
                when (
                    contentPackageImporter.importPackage(download.bytes, entry.versionId, entry.checksumSha256)
                ) {
                    is ContentImportOutcome.Imported, is ContentImportOutcome.Replaced ->
                        PackageResult.Updated(entry.versionId)

                    is ContentImportOutcome.AlreadyUpToDate, is ContentImportOutcome.SkippedOlderVersion ->
                        PackageResult.Skipped(entry.versionId)

                    is ContentImportOutcome.ChecksumConflict, is ContentImportOutcome.Rejected ->
                        PackageResult.Rejected(entry.versionId)
                }
        }

    private suspend fun fetchPackage(versionId: String): PackageDownload =
        try {
            val response = api.getPackage(versionId)
            when {
                !response.isSuccessful ->
                    if (isRetryableHttpStatus(response.code())) {
                        PackageDownload.Abort("package $versionId HTTP ${response.code()}")
                    } else {
                        PackageDownload.Rejected
                    }

                else -> response.body()?.let { streamToTempFile(it) } ?: PackageDownload.Rejected
            }
        } catch (io: IOException) {
            Log.w(TAG, "content package download interrupted for $versionId", io)
            PackageDownload.Abort("package $versionId download interrupted")
        }

    private fun streamToTempFile(body: ResponseBody): PackageDownload {
        val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
        return try {
            writeWithinLimit(body, tempFile)
            PackageDownload.Fetched(tempFile.readBytes())
        } catch (tooLarge: PackageTooLargeException) {
            Log.w(TAG, "content package download rejected", tooLarge)
            PackageDownload.Abort("package exceeds $MAX_PACKAGE_BYTES bytes")
        } catch (io: IOException) {
            Log.w(TAG, "content package download interrupted", io)
            PackageDownload.Abort("package download interrupted")
        } finally {
            tempFile.delete()
        }
    }

    private fun writeWithinLimit(
        body: ResponseBody,
        tempFile: File,
    ) {
        body.byteStream().use { input ->
            tempFile.outputStream().use { output -> copyWithinLimit(input, output) }
        }
    }

    private sealed interface ManifestOutcome {
        data class Success(
            val manifest: RemoteContentManifestDto,
        ) : ManifestOutcome

        data class Failure(
            val result: SyncResult,
        ) : ManifestOutcome
    }

    private sealed interface PackageResult {
        data class Updated(
            val versionId: String,
        ) : PackageResult

        data class Skipped(
            val versionId: String,
        ) : PackageResult

        data class Rejected(
            val versionId: String,
        ) : PackageResult

        data class Abort(
            val reason: String,
        ) : PackageResult
    }

    private sealed interface PackageDownload {
        data class Fetched(
            val bytes: ByteArray,
        ) : PackageDownload

        data class Abort(
            val reason: String,
        ) : PackageDownload

        data object Rejected : PackageDownload
    }

    private companion object {
        const val TAG = "ContentSyncManager"
        const val TEMP_FILE_PREFIX = "content-package-"
        const val TEMP_FILE_SUFFIX = ".json"
    }
}

/** HTTP statuses worth retrying the whole sync for (section 3, 14): request timeout, rate
 * limiting, and any server error. A pure top-level function so it is directly JVM-unit-testable
 * without constructing [ContentSyncManager] itself. */
fun isRetryableHttpStatus(code: Int): Boolean = code in TRANSIENT_HTTP_CODES || code >= HTTP_SERVER_ERROR

private fun copyWithinLimit(
    input: InputStream,
    output: OutputStream,
) {
    var total = 0L
    val buffer = ByteArray(BUFFER_SIZE)
    while (true) {
        val read = input.read(buffer)
        if (read == -1) break
        total += read
        if (total > MAX_PACKAGE_BYTES) throw PackageTooLargeException()
        output.write(buffer, 0, read)
    }
}

private class PackageTooLargeException : IOException("package exceeds $MAX_PACKAGE_BYTES bytes")

private val TRANSIENT_HTTP_CODES = setOf(408, 429)
private const val HTTP_SERVER_ERROR = 500
private const val BUFFER_SIZE = 8 * 1024
private const val MAX_PACKAGE_BYTES = 5L * 1024 * 1024
