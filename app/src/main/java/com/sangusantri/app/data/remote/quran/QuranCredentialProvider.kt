package com.sangusantri.app.data.remote.quran

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.sangusantri.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the Kemenag `username`/`token` credential (ADR 0016,
 * `docs/security/SECURITY_BASELINE.md`).
 *
 * Debug/test builds never touch the native library or a real secret. By default they get an
 * unmistakably fake fixture credential; a developer may optionally set
 * `SANGU_QURAN_DEBUG_API_USERNAME`/`SANGU_QURAN_DEBUG_API_TOKEN` in their untracked
 * `~/.gradle/gradle.properties` (never the tracked project file) to exercise the real Kemenag API
 * from a debug build instead — see [QuranDebugCredentialOverride], whose `debug`/`release`
 * source-set variants read `BuildConfig.QURAN_DEBUG_API_USERNAME`/`QURAN_DEBUG_API_TOKEN` (a field
 * that exists only in the `debug` build type) or a fixed `null`, respectively — kept out of this
 * shared `src/main` file so `compileReleaseKotlin` never needs those debug-only fields to exist
 * (ADR 0016 amendment, 2026-08-09). Release builds reconstruct the credential natively, and only
 * after verifying this running app's release signing-certificate digest against the one embedded
 * at build time. Any mismatch or absent native input fails closed to `null`.
 *
 * Resolved once and held in memory for the process lifetime, never persisted to disk.
 */
@Singleton
class QuranCredentialProvider
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val cachedCredential: QuranCredential? by lazy { resolveCredential() }

    fun getCredential(): QuranCredential? = cachedCredential

    private fun resolveCredential(): QuranCredential? =
        if (BuildConfig.DEBUG) {
            QuranDebugCredentialOverride.resolve() ?: DEBUG_FIXTURE_CREDENTIAL
        } else {
            releaseSigningCertificateSha256()?.let(QuranNativeCredentialBridge::getCredential)
        }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun releaseSigningCertificateSha256(): ByteArray? =
        try {
            val signatures = signingSignatures()
            if (signatures.size != 1) null else sha256(signatures[0].toByteArray())
        } catch (unexpected: Exception) {
            null
        }

    @Suppress("DEPRECATION")
    private fun signingSignatures(): Array<Signature> {
        val packageManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info =
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            info.signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            val info = packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            info.signatures ?: emptyArray()
        }
    }

    private fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

    private companion object {
        val DEBUG_FIXTURE_CREDENTIAL =
            QuranCredential(
                username = "something",
                token = "something",
            )
    }
}
