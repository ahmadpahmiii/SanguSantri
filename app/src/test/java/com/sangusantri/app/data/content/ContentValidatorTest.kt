package com.sangusantri.app.data.content

import com.sangusantri.app.core.validation.Validation
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rejection paths. `CmsApiContractTest` covers the happy path against real captured CMS
 * payloads; what matters here is that a malformed one never reaches Room.
 */
class ContentValidatorTest {
    @Test
    fun validListPassesValidation() {
        assertTrue(ContentValidator.validateList(validList()) is Validation.Valid)
    }

    @Test
    fun listUnsupportedSchemaVersionIsRejected() {
        assertInvalid(ContentValidator.validateList(validList().copy(schemaVersion = 99)))
    }

    @Test
    fun listBlankItemIdIsRejected() {
        assertInvalid(ContentValidator.validateList(listWith(validItem().copy(id = " "))))
    }

    @Test
    fun listDuplicateItemIdsAreRejected() {
        assertInvalid(ContentValidator.validateList(listWith(validItem(), validItem())))
    }

    @Test
    fun listBlankTitleIsRejected() {
        assertInvalid(ContentValidator.validateList(listWith(validItem().copy(title = " "))))
    }

    @Test
    fun listBlankDescriptionIsRejected() {
        assertInvalid(ContentValidator.validateList(listWith(validItem().copy(description = " "))))
    }

    // --- image URL scheme pinning (security review 2026-08-18) ----------------------------------
    //
    // imageUrl is handed straight to Coil. Every case below is an input that would otherwise let a
    // tampered CMS response point the app at a tracker, a cleartext host, or a local file.

    @Test
    fun httpImageUrlIsRejected() {
        assertInvalidImageUrl("http://images.example/tahlil.png")
    }

    @Test
    fun dataUriImageUrlIsRejected() {
        assertInvalidImageUrl("data:image/png;base64,AAAA")
    }

    @Test
    fun fileImageUrlIsRejected() {
        assertInvalidImageUrl("file:///data/data/com.sangusantri.app/databases/sangu.db")
    }

    @Test
    fun schemeOnlyImageUrlIsRejected() {
        assertInvalidImageUrl("https://")
    }

    @Test
    fun httpsImageUrlIsAccepted() {
        val item = validItem().copy(imageUrl = "https://images.example/tahlil.png")

        assertTrue(ContentValidator.validateListItem(item) is Validation.Valid)
    }

    @Test
    fun absentImageUrlIsAccepted() {
        assertTrue(ContentValidator.validateListItem(validItem().copy(imageUrl = null)) is Validation.Valid)
    }

    @Test
    fun validDetailPassesValidation() {
        assertTrue(ContentValidator.validateDetail(validDetail()) is Validation.Valid)
    }

    @Test
    fun detailUnsupportedSchemaVersionIsRejected() {
        assertInvalid(ContentValidator.validateDetail(validDetail().copy(schemaVersion = 99)))
    }

    @Test
    fun detailBlankSourceNameIsRejected() {
        assertInvalid(ContentValidator.validateDetail(validDetail().copy(sourceName = " ")))
    }

    @Test
    fun detailBlankSourceUrlIsRejected() {
        assertInvalid(ContentValidator.validateDetail(validDetail().copy(sourceUrl = " ")))
    }

    @Test
    fun detailEmptyStepsIsRejected() {
        assertInvalid(ContentValidator.validateDetail(validDetail().copy(steps = emptyList())))
    }

    @Test
    fun detailDuplicateStepIdsAreRejected() {
        val detail = validDetail()

        assertInvalid(ContentValidator.validateDetail(detail.copy(steps = detail.steps + detail.steps[0])))
    }

    @Test
    fun detailBlankArabicTextIsRejected() {
        assertInvalidStep(validStep().copy(arabicText = " "))
    }

    @Test
    fun detailBlankTranslationIsRejected() {
        assertInvalidStep(validStep().copy(translation = " "))
    }

    @Test
    fun detailNonPositiveRepeatTargetIsRejected() {
        assertInvalidStep(validStep().copy(repeatTarget = 0))
    }

    @Test
    fun detailAbsentRepeatTargetIsAccepted() {
        val detail = validDetail().copy(steps = listOf(validStep().copy(repeatTarget = null)))

        assertTrue(ContentValidator.validateDetail(detail) is Validation.Valid)
    }

    private fun assertInvalid(result: Validation) {
        assertTrue(result is Validation.Invalid)
    }

    private fun assertInvalidImageUrl(imageUrl: String) {
        assertInvalid(ContentValidator.validateListItem(validItem().copy(imageUrl = imageUrl)))
    }

    private fun assertInvalidStep(step: ContentStepDto) {
        assertInvalid(ContentValidator.validateDetail(validDetail().copy(steps = listOf(step))))
    }

    private fun listWith(vararg items: ContentListItemDto) = validList().copy(items = items.toList())

    private fun validList() = ContentListResponseDto(
        schemaVersion = ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION,
        items = listOf(validItem()),
    )

    private fun validItem() = ContentListItemDto(
        id = "tahlil",
        title = "Tahlil",
        description = "[FIXTURE] Tahlil",
        imageUrl = null,
        category = "Amaliyah",
        order = 1,
    )

    private fun validDetail() = ContentDetailDto(
        schemaVersion = ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION,
        id = "tahlil",
        title = "Tahlil",
        description = "[FIXTURE] Tahlil",
        imageUrl = null,
        category = "Amaliyah",
        order = 1,
        sourceName = "NON-PRODUCTION FIXTURE",
        sourceUrl = "https://example.invalid/fixture",
        steps = listOf(validStep()),
    )

    private fun validStep() =
        ContentStepDto(id = "s1", arabicText = "[FIXTURE-AR]", translation = "[FIXTURE]", repeatTarget = 1)
}
