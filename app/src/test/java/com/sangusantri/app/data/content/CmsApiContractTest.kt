package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import com.sangusantri.app.data.remote.ayat.AyatHariIniValidator
import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
import com.sangusantri.app.domain.model.ContentLayout
import com.sangusantri.app.domain.model.toContentLayout
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/sholawat
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/amaliyah
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/sholawat/salamun-salam
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/amaliyah/tahlil
 *   curl -s https://sangusantri-content-api-v4.vercel.app/api/v1/ayat-hari-ini
 *
 * One detail per category is captured rather than all of them, and the ayat-hari-ini fixture is
 * trimmed to four days whose quotes are deliberately placeholders, not real scripture: what these
 * exist to pin is the *shape* — which fields are null, which are absent — and a second copy of
 * every amaliyah's text in this repository would have nothing keeping it in step with the CMS.
 *
 * `detail-layout-absent.json` and `detail-layout-unknown.json` are the two exceptions: they are
 * hand-written `[FIXTURE]` shapes, never production content and never captured, because the API
 * cannot be made to emit either. They exist because both are reachable in the field — an absent
 * `layout` is every response published before the field existed, and an unrecognised one is a
 * free-text column a future CMS could widen.
 */
class CmsApiContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun cmsListsParseAndValidate() {
        listOf("cmsapi/sholawat.json", "cmsapi/amaliyah.json").forEach { fixture ->
            val response = json.decodeFromString<ContentListResponseDto>(readFixture(fixture))

            assertEquals(ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION, response.schemaVersion)
            assertTrue(fixture, response.items.isNotEmpty())
            assertTrue(fixture, ContentValidator.validateList(response) is ContentValidation.Valid)
        }
    }

    @Test
    fun cmsDetailsParseAndValidate() {
        listOf("cmsapi/amaliyah-tahlil.json", "cmsapi/sholawat-salamun-salam.json").forEach { fixture ->
            val detail = json.decodeFromString<ContentDetailDto>(readFixture(fixture))

            assertEquals(ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION, detail.schemaVersion)
            assertTrue(fixture, detail.steps.isNotEmpty())
            assertTrue(fixture, ContentValidator.validateDetail(detail) is ContentValidation.Valid)
        }
    }

    /**
     * The whole point of the schemaVersion 3 split: the list is fetched on every Beranda resume, so
     * a step edit must not be able to move its bytes. If steps ever reappear here, one correction
     * starts invalidating the whole category's ETag again for every device.
     */
    @Test
    fun cmsListCarriesNoStepsAndNoSource() {
        listOf("cmsapi/sholawat.json", "cmsapi/amaliyah.json").forEach { fixture ->
            val raw = readFixture(fixture)

            listOf("\"steps\"", "\"sourceName\"", "\"sourceUrl\"").forEach { gone ->
                assertFalse("$fixture must not carry $gone", raw.contains(gone))
            }
        }
    }

    /** The detail repeats the list fields so it can draw the whole reader on its own. */
    @Test
    fun cmsDetailIsSelfContained() {
        val detail = json.decodeFromString<ContentDetailDto>(readFixture("cmsapi/amaliyah-tahlil.json"))

        assertEquals("tahlil", detail.id)
        assertTrue(detail.title.isNotBlank())
        assertTrue(detail.description.isNotBlank())
        assertTrue(detail.sourceName.isNotBlank())
        assertTrue(detail.sourceUrl.isNotBlank())
    }

    /** No cover image must arrive as an absent field, never as "" — "" fails the https-only pin. */
    @Test
    fun cmsOmitsImageUrlWhenThereIsNoCoverImage() {
        val response = json.decodeFromString<ContentListResponseDto>(readFixture("cmsapi/sholawat.json"))

        assertNull(response.items.first().imageUrl)
    }

    /** A counted step and an uncounted one in the same item — both must survive the round trip. */
    @Test
    fun cmsCarriesNullRepeatTargetThrough() {
        val detail = json.decodeFromString<ContentDetailDto>(readFixture("cmsapi/amaliyah-tahlil.json"))

        assertTrue(detail.steps.any { it.repeatTarget != null })
        assertTrue(detail.steps.any { it.repeatTarget == null })
    }

    /**
     * Sholawat is the motivating case for the optional counter: no step has one, which is how the
     * app drops Panduan mode for the whole item. If the CMS ever starts sending counts here, the
     * reader silently gains a mode it was never meant to have.
     */
    @Test
    fun cmsSholawatDetailHasNoCountedSteps() {
        val detail = json.decodeFromString<ContentDetailDto>(readFixture("cmsapi/sholawat-salamun-salam.json"))

        assertTrue(detail.steps.isNotEmpty())
        assertTrue(detail.steps.all { it.repeatTarget == null })
    }

    /**
     * `layout` is the reader's whole instruction for arranging an item's steps, and paired verse is
     * indistinguishable from continuous prose in the text itself. Salamun Salam is the qasidah, so
     * it must arrive as bayt with an even step count; Tahlil is not, so it must arrive stacked.
     */
    @Test
    fun cmsCarriesLayoutOnTheDetail() {
        val qasidah = json.decodeFromString<ContentDetailDto>(readFixture("cmsapi/sholawat-salamun-salam.json"))
        assertEquals(ContentLayout.BAYT, qasidah.layout.toContentLayout())
        assertEquals("a bayt layout pairs steps two per row", 0, qasidah.steps.size % 2)

        val amaliyah = json.decodeFromString<ContentDetailDto>(readFixture("cmsapi/amaliyah-tahlil.json"))
        assertEquals(ContentLayout.STACKED, amaliyah.layout.toContentLayout())
    }

    /**
     * The two values that cannot be captured from the API but do reach devices: a detail published
     * before `layout` existed, and one carrying a value this build has never heard of. Both must
     * resolve to STACKED. The asymmetry is the point — stacked reads correctly for any content,
     * two columns cut prose sentences in half, so an unknown value must never become BAYT.
     */
    @Test
    fun cmsUnknownOrAbsentLayoutResolvesToStacked() {
        listOf("cmsapi/detail-layout-absent.json", "cmsapi/detail-layout-unknown.json").forEach { fixture ->
            val detail = json.decodeFromString<ContentDetailDto>(readFixture(fixture))

            assertTrue(fixture, ContentValidator.validateDetail(detail) is ContentValidation.Valid)
            assertEquals(fixture, ContentLayout.STACKED, detail.layout.toContentLayout())
        }
    }

    /** Null and blank take the same road as absent and unrecognised — never a crash, never BAYT. */
    @Test
    fun layoutParserIsTotal() {
        listOf(null, "", "   ", "BAYT?", "stacked", "STACKED", "musammat").forEach { raw ->
            assertEquals("layout=$raw", ContentLayout.STACKED, raw.toContentLayout())
        }
        assertEquals(ContentLayout.BAYT, "bayt".toContentLayout())
        assertEquals(ContentLayout.BAYT, " BAYT ".toContentLayout())
    }

    /**
     * `layout` is a reader concern kept off the list on purpose: the list is fetched on every
     * Beranda resume, so putting it there would make flipping one item's layout re-validate every
     * card in the category — the exact problem the schemaVersion 3 split exists to fix.
     */
    @Test
    fun cmsListCarriesNoLayout() {
        listOf("cmsapi/sholawat.json", "cmsapi/amaliyah.json").forEach { fixture ->
            assertFalse("$fixture must not carry a layout key", readFixture(fixture).contains("\"layout\""))
        }
    }

    /**
     * Retired per-item sync fields. Asserted absent rather than simply unused because
     * `ignoreUnknownKeys` would happily parse a response that still carried them, leaving the app
     * and the CMS disagreeing about what drives an update with nothing to notice.
     */
    @Test
    fun cmsNoLongerSendsVersionOrContentUrl() {
        listOf(
            "cmsapi/sholawat.json",
            "cmsapi/amaliyah.json",
            "cmsapi/amaliyah-tahlil.json",
            "cmsapi/sholawat-salamun-salam.json",
        ).forEach { fixture ->
            val raw = readFixture(fixture)

            listOf("\"version\"", "\"contentUrl\"", "\"isActive\"").forEach { gone ->
                assertFalse("$fixture still carries $gone", raw.contains(gone))
            }
        }
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
