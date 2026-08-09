package com.sangusantri.app.data.remote.quran

/**
 * Release-variant half of the optional real-credential override — see
 * [QuranCredentialProvider]'s doc comment and the `debug` counterpart of this same file. Release
 * builds never call this path at all ([QuranCredentialProvider.resolveCredential] only reaches it
 * under `BuildConfig.DEBUG`); this stub exists purely so `src/main`'s reference to
 * `QuranDebugCredentialOverride` resolves for every build variant, without the debug-only
 * `BuildConfig.QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN` fields ever needing to exist here.
 */
internal object QuranDebugCredentialOverride {
    fun resolve(): QuranCredential? = null
}
