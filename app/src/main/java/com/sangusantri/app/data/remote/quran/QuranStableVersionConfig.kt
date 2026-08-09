package com.sangusantri.app.data.remote.quran

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sangusantri.app.data.remote.config.RemoteConfigFetcher
import javax.inject.Inject

/** Small Remote Config control-plane value for the otherwise fully offline Quran corpus. The
 * number is a monotonic refresh trigger owned by SanguSantri operations, not a version reported by
 * the Kemenag API. */
class QuranStableVersionConfig
@Inject
constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val remoteConfigFetcher: RemoteConfigFetcher,
) {
    suspend fun fetchStableVersion(): Int {
        remoteConfigFetcher.fetchAndActivate()
        return getActivatedStableVersion()
    }

    fun getActivatedStableVersion(): Int =
        remoteConfig
            .getLong(REMOTE_CONFIG_KEY)
            .takeIf { it in BASELINE_VERSION.toLong()..Int.MAX_VALUE.toLong() }
            ?.toInt()
            ?: BASELINE_VERSION

    companion object {
        const val REMOTE_CONFIG_KEY = "quran_stable_version"
        const val BASELINE_VERSION = 1
    }
}
