package com.sangusantri.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sangusantri.app.domain.repository.HomePreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Stores the dismissed resume fingerprint in the app's single canonical preferences DataStore. */
class HomePreferencesRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
) : HomePreferencesRepository {
    override fun observeDismissedResumeFingerprint(): Flow<String?> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.map { preferences -> preferences[DISMISSED_RESUME_FINGERPRINT] }

    override suspend fun dismissResume(fingerprint: String) {
        dataStore.edit { preferences -> preferences[DISMISSED_RESUME_FINGERPRINT] = fingerprint }
    }

    private companion object {
        val DISMISSED_RESUME_FINGERPRINT = stringPreferencesKey("home_dismissed_resume_fingerprint")
    }
}
