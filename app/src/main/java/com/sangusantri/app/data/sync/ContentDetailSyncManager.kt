package com.sangusantri.app.data.sync

import android.util.Log
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.remote.api.ContentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Fetches one item's steps from the CMS (`schemaVersion` 3) when the reader opens it.
 *
 * **This is a refresh, not a load.** The reader has already rendered from Room by the time this
 * returns, and it must stay that way: Room is the source of truth (PRD 12.1), the network only
 * updates it. A failure is therefore not an error the reader sees — offline, or with the CMS down,
 * the cached copy is exactly what the reader wanted anyway.
 *
 * The one case where a failure *is* visible is an item whose detail has never been fetched: it has
 * a row from the list but no steps yet, so there is nothing cached to fall back to. That is the
 * cost of not shipping every step to every device, and it only bites the first time an item is
 * opened.
 *
 * Nothing here decides whether the content changed. The OkHttp cache spares the download with
 * `If-None-Match`, and [ContentImporter.importRemoteDetail] compares against Room before writing.
 */
class ContentDetailSyncManager
@Inject
constructor(
    private val api: ContentApiService,
    private val contentImporter: ContentImporter,
) {
    /**
     * Returns true when Room actually changed, so the caller knows whether to re-read.
     *
     * [isSholawat] picks the route, and comes from [com.sangusantri.app.domain.model.Content.isSholawat]
     * rather than a string comparison here — that predicate already absorbs the transliterations
     * an admin might type ("shalawat", "salawat", …), and getting it wrong would mean asking the
     * wrong category's endpoint and getting a 404 for content that exists.
     */
    suspend fun refresh(
        contentId: String,
        isSholawat: Boolean,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val response =
                try {
                    if (isSholawat) api.getSholawatDetail(contentId) else api.getAmaliyahDetail(contentId)
                } catch (io: IOException) {
                    Log.w(TAG, "detail fetch failed for $contentId", io)
                    return@withContext false
                } catch (malformed: SerializationException) {
                    Log.w(TAG, "detail malformed for $contentId", malformed)
                    return@withContext false
                }

            import(contentId, response)
        }

    @Suppress("ReturnCount")
    private suspend fun import(
        contentId: String,
        response: Response<ContentDetailDto>,
    ): Boolean {
        if (!response.isSuccessful) {
            // A 404 here means the item was unpublished between the list sync and this open.
            // Nothing to do: the next list sync hides it, and the cached copy still reads.
            Log.w(TAG, "detail HTTP ${response.code()} for $contentId")
            return false
        }
        val body = response.body() ?: return false
        if (body.id != contentId) {
            Log.w(TAG, "detail id ${body.id} does not match requested $contentId")
            return false
        }

        return when (val outcome = contentImporter.importRemoteDetail(body)) {
            is ContentImportOutcome.Imported, is ContentImportOutcome.Replaced -> true
            is ContentImportOutcome.Rejected -> {
                Log.w(TAG, "detail rejected for $contentId: ${outcome.reason}")
                false
            }

            else -> false
        }
    }

    private companion object {
        const val TAG = "ContentDetailSync"
    }
}
