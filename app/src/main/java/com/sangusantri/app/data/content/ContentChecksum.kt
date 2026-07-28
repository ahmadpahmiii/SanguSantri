package com.sangusantri.app.data.content

import java.security.MessageDigest

/** Pure SHA-256 helper used to verify a content package's raw bytes before it is parsed (PRD 12.2). */
object ContentChecksum {
    fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
