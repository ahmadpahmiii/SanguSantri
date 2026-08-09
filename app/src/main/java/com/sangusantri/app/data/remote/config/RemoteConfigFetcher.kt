package com.sangusantri.app.data.remote.config

import com.google.firebase.crashlytics.FirebaseCrashlytics
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
class RemoteConfigFetcher
@Inject
constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {
    private val fetchMutex = Mutex()

    suspend fun fetchAndActivate(): Boolean =
        fetchMutex.withLock {
            withTimeoutOrNull(FETCH_TIMEOUT_MILLIS.milliseconds) {
                suspendCancellableCoroutine { continuation ->
                    remoteConfig
                        .fetchAndActivate()
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let(FirebaseCrashlytics.getInstance()::recordException)
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
