package com.sangusantri.app.data.sync

import android.util.Log
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.ContentValidation
import com.sangusantri.app.data.content.ContentValidator
import com.sangusantri.app.data.content.ContentVersionAction
import com.sangusantri.app.data.content.decideContentVersionAction
import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.remote.api.ContentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * One complete remote content-sync execution against the CMS API (ADR 0015):
 * fetch the catalog, compare every item against Room's local versions, fetch and import only
 * genuinely newer/changed content, and return one [SyncResult]. All writes still go through
 * [ContentImporter] — this class never touches a content table directly. Response-size limiting
 * (`docs/security/SECURITY_BASELINE.md`) is enforced transparently at the OkHttp layer
 * ([com.sangusantri.app.data.remote.ResponseSizeLimitInterceptor]), not here — both
 * [ContentApiService] calls return already-parsed, already-size-checked DTOs.
 */
class ContentSyncManager
    @Inject
    constructor(
        private val api: ContentApiService,
        private val contentImporter: ContentImporter,
    ) {
        suspend fun sync(): SyncResult =
            withContext(Dispatchers.IO) {
                when (val outcome = fetchCatalog()) {
                    is CatalogOutcome.Failure -> outcome.result
                    is CatalogOutcome.Success -> processItems(outcome.catalog.items)
                }
            }

        private suspend fun fetchCatalog(): CatalogOutcome =
            try {
                toCatalogOutcome(api.getCatalog())
            } catch (io: IOException) {
                Log.w(TAG, "content catalog fetch failed", io)
                CatalogOutcome.Failure(SyncResult.RetryableFailure("catalog network error"))
            } catch (malformed: SerializationException) {
                Log.w(TAG, "content catalog fetch failed", malformed)
                CatalogOutcome.Failure(SyncResult.PermanentFailure("malformed catalog body"))
            }

        @Suppress("ReturnCount")
        private fun toCatalogOutcome(response: Response<ContentCatalogDto>): CatalogOutcome {
            if (!response.isSuccessful) {
                return CatalogOutcome.Failure(classifyHttpFailure(response.code(), source = "catalog"))
            }
            val catalog =
                response.body() ?: return CatalogOutcome.Failure(SyncResult.PermanentFailure("empty catalog body"))
            val validation = ContentValidator.validateCatalog(catalog)
            if (validation is ContentValidation.Invalid) {
                return CatalogOutcome.Failure(SyncResult.PermanentFailure("invalid catalog: ${validation.reason}"))
            }
            return CatalogOutcome.Success(catalog)
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

        private suspend fun processItems(items: List<ContentCatalogItemDto>): SyncResult {
            val updated = mutableListOf<String>()
            val skipped = mutableListOf<String>()
            val rejected = mutableListOf<String>()

            for (item in items) {
                // Cheap, no-fetch metadata refresh runs for every item regardless of version — this is
                // how isActive/order/title/description/image changes propagate without a content fetch.
                contentImporter.refreshCatalogMetadata(item)

                when (val result = processItem(item)) {
                    is ItemResult.Updated -> updated += result.contentId
                    is ItemResult.Skipped -> skipped += result.contentId
                    is ItemResult.Rejected -> rejected += result.contentId
                    // A retryable content-fetch failure aborts the whole sync (matches manifest-level
                    // retry semantics) — items already updated above stay in Room and are simply
                    // skipped on the next attempt.
                    is ItemResult.Abort -> return SyncResult.RetryableFailure(result.reason)
                }
            }

            return SyncResult.Completed(updated, skipped, rejected)
        }

        private suspend fun processItem(item: ContentCatalogItemDto): ItemResult {
            val localVersion = contentImporter.localVersion(item.id)
            return when (decideContentVersionAction(item.version, localVersion)) {
                ContentVersionAction.SKIP_OLDER, ContentVersionAction.SKIP_UP_TO_DATE -> ItemResult.Skipped(item.id)
                ContentVersionAction.IMPORT -> downloadAndImport(item)
            }
        }

        private suspend fun downloadAndImport(item: ContentCatalogItemDto): ItemResult =
            when (val download = fetchContent(item)) {
                is ContentDownload.Abort -> ItemResult.Abort(download.reason)
                is ContentDownload.Rejected -> ItemResult.Rejected(item.id)
                is ContentDownload.Fetched ->
                    when (contentImporter.importContentFile(item, download.file)) {
                        is ContentImportOutcome.Imported, is ContentImportOutcome.Replaced ->
                            ItemResult.Updated(
                                item.id,
                            )

                        is ContentImportOutcome.SkippedUpToDate, is ContentImportOutcome.SkippedOlderVersion ->
                            ItemResult.Skipped(item.id)

                        is ContentImportOutcome.Rejected -> ItemResult.Rejected(item.id)
                    }
            }

        private suspend fun fetchContent(item: ContentCatalogItemDto): ContentDownload =
            try {
                val response = api.getContent(item.contentUrl)
                when {
                    !response.isSuccessful ->
                        if (isRetryableHttpStatus(response.code())) {
                            ContentDownload.Abort("content ${item.id} HTTP ${response.code()}")
                        } else {
                            ContentDownload.Rejected
                        }

                    else -> response.body()?.let { ContentDownload.Fetched(it) } ?: ContentDownload.Rejected
                }
            } catch (io: IOException) {
                Log.w(TAG, "content download interrupted for ${item.id}", io)
                ContentDownload.Abort("content ${item.id} download interrupted")
            } catch (malformed: SerializationException) {
                Log.w(TAG, "content download malformed for ${item.id}", malformed)
                ContentDownload.Rejected
            }

        private sealed interface CatalogOutcome {
            data class Success(
                val catalog: ContentCatalogDto,
            ) : CatalogOutcome

            data class Failure(
                val result: SyncResult,
            ) : CatalogOutcome
        }

        private sealed interface ItemResult {
            data class Updated(
                val contentId: String,
            ) : ItemResult

            data class Skipped(
                val contentId: String,
            ) : ItemResult

            data class Rejected(
                val contentId: String,
            ) : ItemResult

            data class Abort(
                val reason: String,
            ) : ItemResult
        }

        private sealed interface ContentDownload {
            data class Fetched(
                val file: ContentFileDto,
            ) : ContentDownload

            data class Abort(
                val reason: String,
            ) : ContentDownload

            data object Rejected : ContentDownload
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
