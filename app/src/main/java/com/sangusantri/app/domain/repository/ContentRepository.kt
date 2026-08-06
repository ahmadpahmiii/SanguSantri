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

    suspend fun getContentById(contentId: String): Content?

    suspend fun getContentDetail(contentId: String): ContentDetail?
}
