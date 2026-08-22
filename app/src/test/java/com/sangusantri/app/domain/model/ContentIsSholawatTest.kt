package com.sangusantri.app.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The category is free text typed into the CMS and matched against a constant compiled into the
 * app. Getting that match wrong fails twice over — the item leaks into the Amaliyah surfaces AND
 * Beranda's Sholawat tile stays disabled, because both read this one predicate.
 */
class ContentIsSholawatTest {
    @Test
    fun acceptsEveryCommonTransliteration() {
        listOf("Shalawat", "Sholawat", "Salawat", "Solawat").forEach { spelling ->
            assertTrue(spelling, content(category = spelling).isSholawat)
        }
    }

    @Test
    fun ignoresCaseAndSurroundingWhitespace() {
        assertTrue(content(category = "  SHOLAWAT ").isSholawat)
        assertTrue(content(category = "sholawat").isSholawat)
    }

    @Test
    fun amaliyahCategoriesAreNotSholawat() {
        listOf("Amaliyah", "Tahlil dan Doa", "Doa").forEach { category ->
            assertFalse(category, content(category = category).isSholawat)
        }
    }

    @Test
    fun absentCategoryIsNotSholawat() {
        assertFalse(content(category = null).isSholawat)
    }

    private fun content(category: String?) =
        Content(
            id = "id",
            title = "[FIXTURE]",
            description = "[FIXTURE]",
            imageUrl = null,
            category = category,
            version = 1,
            order = 0,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "https://example.com",
        )
}
