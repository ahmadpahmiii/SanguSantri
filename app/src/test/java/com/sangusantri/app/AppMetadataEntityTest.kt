package com.sangusantri.app

import com.sangusantri.app.data.local.entity.AppMetadataEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppMetadataEntityTest {
    @Test
    fun `entities with the same key and value are equal`() {
        val first = AppMetadataEntity(key = "schema_version", value = "1", updatedAtEpochMillis = 100L)
        val second = AppMetadataEntity(key = "schema_version", value = "1", updatedAtEpochMillis = 100L)

        assertEquals(first, second)
    }

    @Test
    fun `copy with a new value produces a distinct entity`() {
        val original = AppMetadataEntity(key = "schema_version", value = "1", updatedAtEpochMillis = 100L)

        val updated = original.copy(value = "2", updatedAtEpochMillis = 200L)

        assertNotEquals(original, updated)
        assertEquals("schema_version", updated.key)
    }
}
