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
 * Debug/test builds never touch the native library or a real secret — they always get an
 * unmistakably fake fixture credential. Release builds reconstruct the credential natively, and
 * only after verifying this running app's release signing-certificate digest against the one
 * embedded at build time. Any mismatch or absent native input fails closed to `null`.
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
            DEBUG_FIXTURE_CREDENTIAL
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
        val DEBUG_FIXTURE_CREDENTIAL = QuranCredential(
            username = "pahmi9",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXNzd29yZCI6Ijc3NDY1YjNhMDhkNzJjZTJiNTc1NTEwNDVhNmFiMTFiIiwiaWF0IjoxNzg2MTE1NjgyfQ.vh4wr_8qXzsgCirCZjnRv6bqQmctd0duJxkGxe3O_oA"
        )
    }
}
