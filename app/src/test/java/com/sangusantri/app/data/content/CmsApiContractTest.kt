package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses and validates real captured responses from the CMS API (`cms/api`, deployed on Vercel).
 *
 * The DTOs alone do not catch a contract break: an over-permissive field still parses, and the
 * validator then rejects the catalog at runtime, which the sync worker reports as a *successful*
 * run with nothing imported. That failure is invisible short of reading the device database — it
 * cost a full debug cycle when the API emitted `"imageUrl": ""` instead of omitting the field. So
 * the fixtures are captured verbatim from the API and both layers run over them here.
 *
 * Re-capture with:
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/catalog
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/content/tahlil
 */
class CmsApiContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun cmsCatalogParsesAndValidates() {
        val catalog = json.decodeFromString<ContentCatalogDto>(readFixture("cmsapi/catalog.json"))

        assertEquals(ContentValidator.SUPPORTED_SCHEMA_VERSION, catalog.schemaVersion)
        assertTrue(catalog.items.isNotEmpty())
        assertTrue(ContentValidator.validateCatalog(catalog) is ContentValidation.Valid)
    }

    /** No cover image must arrive as an absent field, never as "" — "" fails the https-only pin. */
    @Test
    fun cmsCatalogOmitsImageUrlWhenThereIsNoCoverImage() {
        val catalog = json.decodeFromString<ContentCatalogDto>(readFixture("cmsapi/catalog.json"))

        assertNull(catalog.items.first().imageUrl)
    }

    /** contentUrl is origin-relative so Retrofit resolves it against the configured base URL. */
    @Test
    fun cmsCatalogContentUrlStaysOnTheConfiguredOrigin() {
        val catalog = json.decodeFromString<ContentCatalogDto>(readFixture("cmsapi/catalog.json"))

        catalog.items.forEach { item ->
            assertTrue(item.contentUrl, ContentValidator.isOriginRelativeContentPath(item.contentUrl))
        }
    }

    @Test
    fun cmsContentFileParsesAndValidates() {
        val file = json.decodeFromString<ContentFileDto>(readFixture("cmsapi/content-tahlil.json"))

        assertTrue(ContentValidator.validateContentFile(file) is ContentValidation.Valid)
    }

    /** A counted step and an uncounted one in the same file — both must survive the round trip. */
    @Test
    fun cmsContentFileCarriesNullRepeatTargetThrough() {
        val file = json.decodeFromString<ContentFileDto>(readFixture("cmsapi/content-tahlil.json"))

        assertNotNull(file.steps[0].repeatTarget)
        assertNull(file.steps[1].repeatTarget)
    }

    private fun readFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) { "missing fixture $path" }
            .use { it.readBytes().decodeToString() }
}
