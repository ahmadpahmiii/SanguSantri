package com.sangusantri.app.data.remote

import android.content.Context
import android.util.Log
import com.sangusantri.app.data.remote.api.ContentApiService
import com.sangusantri.app.data.remote.dto.RemoteContentManifestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Talks to [ContentApiService] and returns typed outcomes only (section 9) — Retrofit's
 * [Response]/[ResponseBody] and raw HTTP exceptions never cross this boundary; only
 * [com.sangusantri.app.data.sync.ContentSyncCoordinator] calls this class. Package bytes are
 * streamed into a size-limited temporary cache file rather than assumed to always stay tiny, and
 * the temporary file is always deleted once its bytes are read back, success or failure.
 */
class ContentRemoteDataSource
@Inject
constructor(
    private val api: ContentApiService,
    @param:ApplicationContext private val context: Context,
) {
    suspend fun fetchManifest(storedEtag: String?): ManifestFetchOutcome =
        withContext(Dispatchers.IO) {
            runCatching { api.getManifest(storedEtag) }
                .fold(
                    onSuccess = { toManifestOutcome(it) },
                    onFailure = {
                        Log.w(TAG, "content manifest fetch failed", it)
                        ManifestFetchOutcome.Failed(classify(it))
                    },
                )
        }

    private fun toManifestOutcome(response: Response<RemoteContentManifestDto>): ManifestFetchOutcome =
        when {
            response.code() == HTTP_NOT_MODIFIED -> ManifestFetchOutcome.NotModified
            response.isSuccessful ->
                response.body()?.let { ManifestFetchOutcome.Fetched(it, response.headers()["ETag"]) }
                    ?: ManifestFetchOutcome.Failed(RemoteContentFailure.MalformedBody("empty manifest body"))

            else -> ManifestFetchOutcome.Failed(RemoteContentFailure.HttpStatus(response.code()))
        }

    suspend fun fetchPackage(versionId: String): PackageFetchOutcome =
        withContext(Dispatchers.IO) {
            runCatching { api.getPackage(versionId) }
                .fold(
                    onSuccess = { toPackageOutcome(it) },
                    onFailure = {
                        Log.w(TAG, "content package fetch failed for $versionId", it)
                        PackageFetchOutcome.Failed(classify(it))
                    },
                )
        }

    @Suppress("ReturnCount")
    private fun toPackageOutcome(response: Response<ResponseBody>): PackageFetchOutcome {
        if (!response.isSuccessful) {
            return PackageFetchOutcome.Failed(RemoteContentFailure.HttpStatus(response.code()))
        }
        val body =
            response.body()
                ?: return PackageFetchOutcome.Failed(RemoteContentFailure.MalformedBody("empty package body"))
        return streamToTempFile(body)
    }

    private fun streamToTempFile(body: ResponseBody): PackageFetchOutcome {
        val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, context.cacheDir)
        return try {
            writeWithinLimit(body, tempFile)
            PackageFetchOutcome.Fetched(tempFile.readBytes())
        } catch (tooLarge: PackageTooLargeException) {
            Log.w(TAG, "content package download rejected", tooLarge)
            PackageFetchOutcome.Failed(RemoteContentFailure.PayloadTooLarge(MAX_PACKAGE_BYTES))
        } catch (io: IOException) {
            Log.w(TAG, "content package download interrupted", io)
            PackageFetchOutcome.Failed(RemoteContentFailure.NoConnectivityOrTimeout)
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

    private fun classify(cause: Throwable): RemoteContentFailure =
        when (cause) {
            is IOException -> RemoteContentFailure.NoConnectivityOrTimeout
            else -> RemoteContentFailure.MalformedBody(cause.message ?: cause.javaClass.simpleName)
        }

    private class PackageTooLargeException : IOException("package exceeds $MAX_PACKAGE_BYTES bytes")

    private companion object {
        const val TAG = "ContentRemoteDataSource"
        const val HTTP_NOT_MODIFIED = 304
        const val BUFFER_SIZE = 8 * 1024
        const val MAX_PACKAGE_BYTES = 5L * 1024 * 1024
        const val TEMP_FILE_PREFIX = "content-package-"
        const val TEMP_FILE_SUFFIX = ".json"
    }
}
