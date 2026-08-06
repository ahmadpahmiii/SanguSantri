package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.StepProgress

/** Reads and writes Guided Reader session/counter progress via Room, keyed by content id. */
interface GuidedReadingRepository {
    suspend fun getSession(contentId: String): GuidedReadingSession?

    suspend fun saveSession(session: GuidedReadingSession)

    suspend fun getStepProgress(contentId: String): List<StepProgress>

    suspend fun saveStepProgress(progress: StepProgress)
}
