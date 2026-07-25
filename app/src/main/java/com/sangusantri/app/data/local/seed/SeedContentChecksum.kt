package com.sangusantri.app.data.local.seed

import java.security.MessageDigest

/** Pure SHA-256 helper used to verify bundled content packages before they are parsed (PRD 12.2). */
object SeedContentChecksum {
    fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
