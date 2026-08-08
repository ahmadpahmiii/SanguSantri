package com.sangusantri.app.data.remote.quran

/**
 * JNI boundary to the native credential-reconstruction library (`app/src/main/cpp/`, ADR 0016).
 * Only ever invoked for release builds — [QuranCredentialProvider] short-circuits to a fixed fake
 * credential for `BuildConfig.DEBUG`, so debug/test builds never load this library or need the NDK
 * toolchain's output present on the device.
 */
internal object QuranNativeCredentialBridge {
    private val libraryAvailable: Boolean by lazy {
        runCatching { System.loadLibrary(LIBRARY_NAME) }.isSuccess
    }

    /**
     * Returns the reconstructed credential only when [signingCertificateSha256] matches the
     * expected release signing certificate embedded at build time; returns `null` on any mismatch,
     * missing library, or missing build-time secret. Always fails closed, never throws, and never
     * logs the digest or credential material either side of the comparison.
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    fun getCredential(signingCertificateSha256: ByteArray): QuranCredential? {
        if (!libraryAvailable) return null
        val encoded =
            try {
                nativeGetCredential(signingCertificateSha256)
            } catch (unexpected: Exception) {
                null
            } ?: return null
        val separatorIndex = encoded.indexOf(CREDENTIAL_SEPARATOR)
        if (separatorIndex < 0) return null
        return QuranCredential(
            username = encoded.substring(0, separatorIndex),
            token = encoded.substring(separatorIndex + 1),
        )
    }

    private external fun nativeGetCredential(signingCertificateSha256: ByteArray): String?

    private const val LIBRARY_NAME = "qurancredential"

    // U+0001 (start-of-heading control char) — never appears in a real username/token, safe as a
    // plain delimiter for the single combined string the JNI boundary returns.
    private const val CREDENTIAL_SEPARATOR = '\u0001'
}
