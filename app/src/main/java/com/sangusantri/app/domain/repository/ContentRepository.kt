package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import kotlinx.coroutines.flow.Flow

/**
 * Read access to the local content catalogue. Room is the source of truth
 * (PRD 12.1) — implementations must never read from the network directly.
 */
interface ContentRepository {
    /** Active catalog items only, ordered for Beranda display (ADR 0015). */
    fun observeActiveContent(): Flow<List<Content>>

    /**
     * Ids of items whose *steps* contain [query] — the "cari per ayat" half of catalogue search,
     * combined by callers with the title/description match they can do in memory. A blank query
     * matches nothing here (it is not "match everything"; the caller's title filter already lets
     * every item through).
     *
     * Only cached steps are searchable. An item whose detail has never been opened has no step
     * rows yet — `ContentDetailSyncManager` fetches them on first open — so it can be found by
     * title but not yet by its text.
     */
    fun observeContentIdsMatchingStepText(query: String): Flow<List<String>>

    suspend fun getContentById(contentId: String): Content?

    suspend fun getContentDetail(contentId: String): ContentDetail?
}
