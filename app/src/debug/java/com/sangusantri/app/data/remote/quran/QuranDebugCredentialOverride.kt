package com.sangusantri.app.data.remote.quran

import com.sangusantri.app.BuildConfig

/**
 * Debug-variant half of the optional real-credential override (ADR 0016 amendment, 2026-08-09) —
 * see [QuranCredentialProvider]'s doc comment. `BuildConfig.QURAN_DEBUG_API_USERNAME`/
 * `QURAN_DEBUG_API_TOKEN` exist only for the `debug` build type (`app/build.gradle.kts`), which is
 * exactly why this lives in `src/debug/`, not `src/main/`: the `release` counterpart below never
 * references those fields, so `compileReleaseKotlin` never needs them to exist.
 */
internal object QuranDebugCredentialOverride {
    fun resolve(): QuranCredential? {
        val username = BuildConfig.QURAN_DEBUG_API_USERNAME
        val token = BuildConfig.QURAN_DEBUG_API_TOKEN
        return if (username.isNotBlank() && token.isNotBlank()) {
            QuranCredential(username = username, token = token)
        } else {
            null
        }
    }
}
