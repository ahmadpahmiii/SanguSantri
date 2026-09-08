package com.sangusantri.app.domain.repository

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.result.Resource
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import kotlinx.coroutines.flow.Flow

/**
 * The content catalogue, offline-first.
 *
 * Room is the source of truth (PRD 12.1) and everything emitted here comes from it — but this
 * interface *owns* the refresh that keeps Room current, and that is the correction over the
 * previous design. The old contract said "implementations must never read from the network", which
 * pushed the CMS client into `ContentSyncManager`/`ContentDetailSyncManager` and from there into
 * three ViewModels. The UI layer ended up driving cache invalidation, so every screen re-invented
 * "read cache, fire refresh, maybe re-read", and Beranda had to keep a `contentSyncFailed` flag of
 * its own to know whether an empty list meant "nothing published" or "never reached the CMS".
 *
 * A [Resource] answers that question here instead, once, for everyone.
 */
interface ContentRepository {
    /**
     * Active catalogue items, ordered for display: cached rows immediately, CMS refresh in
     * parallel, re-emitted if the refresh changed anything.
     *
     * Safe to collect on every screen resume — the two category listings carry no steps, so their
     * `ETag`s do not move when someone corrects a word inside an item, and the common case costs
     * two `304`s and no body.
     */
    fun observeActiveContent(): Flow<Resource<List<Content>>>

    /**
     * One item with its steps: the cached copy immediately, its CMS detail refreshed in parallel.
     *
     * [Resource.Error] with data present is an offline reader whose cached copy still reads
     * perfectly — readers should render it and say nothing. [Resource.Error] with no data is the
     * case that genuinely blocks: an item created from a list sync whose detail has never been
     * fetched, opened for the first time with no network.
     */
    fun observeContentDetail(contentId: String): Flow<Resource<ContentDetail>>

    /**
     * Ids of items whose *steps* contain [query] — the "cari per ayat" half of catalogue search,
     * combined by callers with the title/description match they can do in memory. A blank query
     * matches nothing here (it is not "match everything"; the caller's title filter already lets
     * every item through).
     *
     * Only cached steps are searchable. An item whose detail has never been opened has no step
     * rows yet, so it can be found by title but not yet by its text.
     */
    fun observeContentIdsMatchingStepText(query: String): Flow<List<String>>

    /** Cache only, never the network — for callers that must not stall or spend data on a refresh,
     * such as an alarm receiver building a notification title. */
    suspend fun getContentById(contentId: String): Content?

    /** Cache only, never the network. See [getContentById]. */
    suspend fun getCachedContentDetail(contentId: String): ContentDetail?

    /**
     * Refresh the catalogue and report whether it worked — for the background sync worker, which
     * has no UI to render and needs only [ApiResult.Failure.isRetryable] to decide its retry.
     */
    suspend fun refreshCatalogue(): ApiResult<Unit>
}
