package com.sangusantri.app.data.sync

import android.util.Log
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.ContentValidation
import com.sangusantri.app.data.content.ContentValidator
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import com.sangusantri.app.data.remote.api.ContentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * One complete list sync against the CMS API (`schemaVersion` 3): fetch both category listings,
 * update the catalogue Beranda draws from, hide anything the CMS stopped publishing, and return
 * one [SyncResult].
 *
 * **Metadata only — this never fetches steps.** Steps belong to [ContentDetailSyncManager], which
 * runs when the reader opens an item. That split is why this can run on every Beranda resume: the
 * two listings are a few hundred bytes each, and their `ETag`s do not move when someone corrects a
 * word inside an item, so the common resume costs two `304`s and nothing else.
 *
 * All writes go through [ContentImporter] — this class never touches a content table directly.
 */
open class ContentSyncManager
@Inject
constructor(
    private val api: ContentApiService,
    private val contentImporter: ContentImporter,
) {
    open suspend fun sync(): SyncResult =
        withContext(Dispatchers.IO) {
            // Both listings are fetched before anything is written. A half-fetched sync must not
            // deactivate the half it never saw, and aborting early leaves Room exactly as it was.
            when (val sholawat = fetchList("sholawat") { api.getSholawatList() }) {
                is ListOutcome.Failure -> sholawat.result
                is ListOutcome.Success ->
                    when (val amaliyah = fetchList("amaliyah") { api.getAmaliyahList() }) {
                        is ListOutcome.Failure -> amaliyah.result
                        is ListOutcome.Success ->
                            importAll(sholawat.response.items + amaliyah.response.items)
                    }
            }
        }

    private suspend fun fetchList(
        name: String,
        call: suspend () -> Response<ContentListResponseDto>,
    ): ListOutcome =
        try {
            toListOutcome(name, call())
        } catch (io: IOException) {
            Log.w(TAG, "$name list fetch failed", io)
            ListOutcome.Failure(SyncResult.RetryableFailure("$name network error"))
        } catch (malformed: SerializationException) {
            Log.w(TAG, "$name list fetch failed", malformed)
            ListOutcome.Failure(SyncResult.PermanentFailure("malformed $name body"))
        }

    @Suppress("ReturnCount")
    private fun toListOutcome(
        name: String,
        response: Response<ContentListResponseDto>,
    ): ListOutcome {
        if (!response.isSuccessful) {
            return ListOutcome.Failure(classifyHttpFailure(response.code(), source = name))
        }
        val body =
            response.body() ?: return ListOutcome.Failure(SyncResult.PermanentFailure("empty $name body"))
        val validation = ContentValidator.validateList(body)
        if (validation is ContentValidation.Invalid) {
            return ListOutcome.Failure(SyncResult.PermanentFailure("invalid $name: ${validation.reason}"))
        }
        return ListOutcome.Success(body)
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

    private suspend fun importAll(items: List<ContentListItemDto>): SyncResult {
        val updated = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val rejected = mutableListOf<String>()

        for (item in items) {
            when (contentImporter.importListItem(item)) {
                is ContentImportOutcome.Imported, is ContentImportOutcome.Replaced -> updated += item.id
                is ContentImportOutcome.SkippedUpToDate -> skipped += item.id

                is ContentImportOutcome.Rejected -> rejected += item.id
            }
        }

        // Runs only here, after both listings parsed and validated: an item missing because a
        // request failed must never be mistaken for one the CMS unpublished.
        val hidden = contentImporter.deactivateAbsent(items.map { it.id })
        if (hidden > 0) {
            Log.i(TAG, "hid $hidden item(s) no longer published by the CMS")
        }

        return SyncResult.Completed(updated, skipped, rejected)
    }

    private sealed interface ListOutcome {
        data class Success(
            val response: ContentListResponseDto,
        ) : ListOutcome

        data class Failure(
            val result: SyncResult,
        ) : ListOutcome
    }

    private companion object {
        const val TAG = "ContentSyncManager"
    }
}

/** HTTP statuses worth retrying the whole sync for: request timeout, rate limiting, and any
 * server error. A pure top-level function so it is directly JVM-unit-testable without
 * constructing [ContentSyncManager] itself. */
fun isRetryableHttpStatus(code: Int): Boolean = code in TRANSIENT_HTTP_CODES || code >= HTTP_SERVER_ERROR

private val TRANSIENT_HTTP_CODES = setOf(408, 429)
private const val HTTP_SERVER_ERROR = 500
