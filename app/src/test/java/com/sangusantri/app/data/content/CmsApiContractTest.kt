package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.remote.ayat.AyatHariIniValidator
import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
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
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/ayat-hari-ini
 *
 * The ayat-hari-ini fixture is trimmed to four days and its quotes are deliberately placeholders,
 * not real scripture: what it exists to pin is the *shape* — which fields are null, which are
 * absent — and capturing real Qur'an text into a test resource would create a second copy of it in
 * this repository with nothing keeping it in step with the CMS.
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

    /**
     * Schema version 2 of the quote-of-the-day route. The version number is the safety mechanism:
     * version 1 published a `surah`/`ayat` reference and this app resolved the words locally, so
     * the two shapes share no fields at all. An app that meets the wrong one must reject it and
     * keep its cache rather than parse it half-way.
     */
    @Test
    fun cmsAyatHariIniParsesAndValidates() {
        val schedule = json.decodeFromString<AyatHariIniScheduleDto>(readFixture("cmsapi/ayat-hari-ini.json"))

        assertEquals(AyatHariIniValidator.SUPPORTED_SCHEMA_VERSION, schedule.schemaVersion)
        assertEquals(schedule.items.size, AyatHariIniValidator.validate(schedule.items).size)
    }

    /**
     * A quote need not be in Arabic and need not have an English translation. Both must survive as
     * null rather than as "" — an empty string would render as a blank script line on the card, and
     * for `en` would blank the whole quote on an English-language device.
     */
    @Test
    fun cmsAyatHariIniCarriesAbsentArabicAndEnglishThrough() {
        val schedule = json.decodeFromString<AyatHariIniScheduleDto>(readFixture("cmsapi/ayat-hari-ini.json"))
        val selections = AyatHariIniValidator.validate(schedule.items)

        val withArabic = selections.first { it.arabic != null }
        assertNotNull(withArabic.translationEn)

        val withoutArabic = selections.first { it.arabic == null }
        assertNull(withoutArabic.translationEn)
        assertTrue(withoutArabic.translationId.isNotBlank())
    }

    /** Every published quote carries a citation — the CMS enforces it with a NOT NULL column, and
     * the validator drops any row that somehow arrives without one. */
    @Test
    fun cmsAyatHariIniAlwaysCarriesASourceLabel() {
        val schedule = json.decodeFromString<AyatHariIniScheduleDto>(readFixture("cmsapi/ayat-hari-ini.json"))

        AyatHariIniValidator.validate(schedule.items).forEach {
            assertTrue(it.sourceLabel.isNotBlank())
        }
    }

    /** Dates are plain YYYY-MM-DD with no timezone, one per day, ascending. */
    @Test
    fun cmsAyatHariIniPublishesOneAscendingDatePerDay() {
        val schedule = json.decodeFromString<AyatHariIniScheduleDto>(readFixture("cmsapi/ayat-hari-ini.json"))
        val dates = AyatHariIniValidator.validate(schedule.items).map { it.date }

        assertEquals(dates.distinct(), dates)
        assertEquals(dates.sorted(), dates)
    }

    private fun readFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) { "missing fixture $path" }
            .use { it.readBytes().decodeToString() }
}
