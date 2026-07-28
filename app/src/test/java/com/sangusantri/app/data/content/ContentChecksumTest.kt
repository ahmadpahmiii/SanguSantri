package com.sangusantri.app.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ContentChecksumTest {
    @Test
    fun sha256HexMatchesTheKnownDigestOfAbc() {
        val result = ContentChecksum.sha256Hex("abc".toByteArray(Charsets.UTF_8))

        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            result,
        )
    }

    @Test
    fun sha256HexMatchesTheKnownDigestOfEmptyBytes() {
        val result = ContentChecksum.sha256Hex(ByteArray(0))

        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            result,
        )
    }

    @Test
    fun sha256HexChangesWhenInputChanges() {
        val first = ContentChecksum.sha256Hex("abc".toByteArray(Charsets.UTF_8))
        val second = ContentChecksum.sha256Hex("abd".toByteArray(Charsets.UTF_8))

        assertNotEquals(first, second)
    }
}
