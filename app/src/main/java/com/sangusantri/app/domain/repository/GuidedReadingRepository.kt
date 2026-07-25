package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.StepProgress

/** Reads and writes Guided Reader session/counter progress via Room, keyed by content version id. */
interface GuidedReadingRepository {
    suspend fun getSession(versionId: String): GuidedReadingSession?

    suspend fun saveSession(session: GuidedReadingSession)

    suspend fun getStepProgress(versionId: String): List<StepProgress>

    suspend fun saveStepProgress(progress: StepProgress)
}
