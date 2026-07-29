package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the Aktivitas (`0.0.3`) amaliyah-completion-event log via Room. Tasbih activity
 * data is a separate, already-established concern owned by [TasbihRepository] — reused directly by
 * consumers of this repository, not duplicated or wrapped here
 * (`docs/engineering/ARCHITECTURE.md`'s per-concern-repository convention).
 */
interface ActivityRepository {
    /**
     * Records one completion event. [startedAtEpochMillis]/[completedAtEpochMillis] are real
     * timestamps the caller observed — this computes and stores the duration snapshot, it never
     * invents one.
     */
    suspend fun recordCompletion(
        amaliyahSlug: String,
        amaliyahTitleId: String,
        versionNumber: Int,
        startedAtEpochMillis: Long,
        completedAtEpochMillis: Long,
    )

    fun observeCompletions(): Flow<List<AmaliyahCompletionEvent>>
}
