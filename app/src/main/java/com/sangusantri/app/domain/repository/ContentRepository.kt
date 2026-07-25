package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
import kotlinx.coroutines.flow.Flow

/**
 * Read access to the local amaliyah catalogue. Room is the source of truth
 * (PRD 12.1) — implementations must never read from the network directly.
 */
interface ContentRepository {
    fun observeAmaliyah(): Flow<List<Amaliyah>>

    suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail?
}
