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
 * Resolves the Kemenag `username`/`token` credential (ADR 0016, `docs/security/SECURITY_BASELINE.md`).
 *
 * Debug/test builds never touch the native library or a real secret — they always get an
 * unmistakably fake fixture credential (`docs/product/QURAN_PRD.md` §9: "Debug and automated tests
 * use fakes ... and never require production credentials"). Release builds reconstruct the
 * credential natively, and only after verifying this running app's own release signing-certificate
 * digest against the one embedded at build time — any mismatch, absent native input, or unexpected
 * signer count fails closed to `null`, never a crash and never a logged secret.
 *
 * Resolved once and held in memory for the process lifetime (the shortest-lived value practical
 * given every Kemenag request needs it), never persisted to disk.
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
            DEBUG_FIXTURE_CREDENTIAL
        } else {
            releaseSigningCertificateSha256()?.let(QuranNativeCredentialBridge::getCredential)
        }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun releaseSigningCertificateSha256(): ByteArray? =
        try {
            val signatures = signingSignatures()
            // Exactly one release signer is expected; zero or multiple is treated as untrusted
            // rather than guessing which signature is authoritative.
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
            QuranCredential(username = "debug-fixture-username", token = "debug-fixture-token")
    }
}
