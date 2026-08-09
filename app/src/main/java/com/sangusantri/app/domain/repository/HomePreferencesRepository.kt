package com.sangusantri.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Persists Beranda-only preferences without coupling them to reader appearance settings. */
interface HomePreferencesRepository {
    fun observeDismissedResumeFingerprint(): Flow<String?>

    suspend fun dismissResume(fingerprint: String)
}
