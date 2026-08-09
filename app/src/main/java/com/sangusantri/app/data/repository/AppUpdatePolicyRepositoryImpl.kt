package com.sangusantri.app.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sangusantri.app.data.remote.config.RemoteConfigFetcher
import com.sangusantri.app.data.remote.update.AppUpdatePolicyDto
import com.sangusantri.app.data.remote.update.toDomain
import com.sangusantri.app.domain.model.AppUpdatePolicy
import com.sangusantri.app.domain.repository.AppUpdatePolicyRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Reads the `in_app_update` Remote Config parameter (ADR 0017). Fails open on any fetch or parse
 * failure — a missing/malformed policy must never crash the app or block the user, so every
 * failure path returns `null` (never thrown) after recording it, never silently.
 */
class AppUpdatePolicyRepositoryImpl
@Inject
constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val remoteConfigFetcher: RemoteConfigFetcher,
    private val json: Json,
) : AppUpdatePolicyRepository {
    override suspend fun fetchPolicy(): AppUpdatePolicy? {
        remoteConfigFetcher.fetchAndActivate()

        val raw = remoteConfig.getString(REMOTE_CONFIG_KEY)
        if (raw.isBlank()) return null

        return try {
            json.decodeFromString<AppUpdatePolicyDto>(raw).toDomain()
        } catch (e: SerializationException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    private companion object {
        const val REMOTE_CONFIG_KEY = "in_app_update"
    }
}
