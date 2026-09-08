package com.sangusantri.app.data.remote.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/** Serialises the app's Remote Config fetch-and-activate calls so independent consumers share one
 * network refresh instead of racing duplicate Firebase requests. Activated cached/default values
 * remain readable when the fetch times out or fails. */
@Singleton
class RemoteConfigFetcher @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) {
    private val fetchMutex = Mutex()

    suspend fun fetchAndActivate(): Boolean = fetchMutex.withLock {
        withTimeoutOrNull(FETCH_TIMEOUT_MILLIS.milliseconds) {
            suspendCancellableCoroutine { continuation ->
                remoteConfig
                    .fetchAndActivate()
                    .addOnCompleteListener { task ->
                        // A failed fetch is not an error condition: the caller falls back to the
                        // activated cached values, and every failure seen in production so far was
                        // the network rather than the app (EAI_NODATA offline, gateway timeouts,
                        // connection resets, Installations unavailable). Recording them made
                        // Crashlytics a connectivity log; a malformed *payload* is still recorded,
                        // in AppUpdatePolicyRepositoryImpl, because that one is ours to fix.
                        if (!task.isSuccessful) {
                            Log.w("RemoteConfig", "fetchAndActivate failed", task.exception)
                        }
                        if (continuation.isActive) {
                            continuation.resume(task.isSuccessful && task.result == true)
                        }
                    }
            }
        } ?: false
    }

    private companion object {
        const val FETCH_TIMEOUT_MILLIS = 5_000L
    }
}
