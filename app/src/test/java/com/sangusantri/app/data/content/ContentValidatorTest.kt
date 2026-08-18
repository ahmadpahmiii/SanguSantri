package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentStepDto
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidatorTest {
    @Test
    fun validCatalogPassesValidation() {
        val result = ContentValidator.validateCatalog(validCatalog())

        assertTrue(result is ContentValidation.Valid)
    }

    @Test
    fun catalogUnsupportedSchemaVersionIsRejected() {
        val result = ContentValidator.validateCatalog(validCatalog().copy(schemaVersion = 99))

        assertInvalid(result)
    }

    @Test
    fun catalogBlankItemIdIsRejected() {
        val catalog = validCatalog()
        val result = ContentValidator.validateCatalog(catalog.copy(items = listOf(catalog.items[0].copy(id = " "))))

        assertInvalid(result)
    }

    @Test
    fun catalogDuplicateItemIdsAreRejected() {
        val catalog = validCatalog()
        val result = ContentValidator.validateCatalog(catalog.copy(items = catalog.items + catalog.items[0]))

        assertInvalid(result)
    }

    @Test
    fun catalogNonPositiveVersionIsRejected() {
        val catalog = validCatalog()
        val result =
            ContentValidator.validateCatalog(catalog.copy(items = listOf(catalog.items[0].copy(version = 0))))

        assertInvalid(result)
    }

    @Test
    fun catalogBlankContentUrlIsRejected() {
        val catalog = validCatalog()
        val result =
            ContentValidator.validateCatalog(catalog.copy(items = listOf(catalog.items[0].copy(contentUrl = ""))))

        assertInvalid(result)
    }

    // --- catalog URL origin pinning (security review 2026-08-18) --------------------------------
    //
    // contentUrl reaches Retrofit as an @Url, where an absolute value replaces the configured base
    // URL outright, and reaches AssetManager.open in the bundled pipeline. Every case below is an
    // input that would otherwise pull religious content off the vetted origin or out of the asset
    // directory.

    @Test
    fun catalogAbsoluteContentUrlIsRejected() {
        assertInvalid(catalogWithContentUrl("https://elsewhere.example/tahlil-v1.json"))
    }

    @Test
    fun catalogProtocolRelativeContentUrlIsRejected() {
        assertInvalid(catalogWithContentUrl("//elsewhere.example/content/tahlil-v1.json"))
    }

    @Test
    fun catalogTraversingContentUrlIsRejected() {
        assertInvalid(catalogWithContentUrl("/content/../../etc/passwd"))
    }

    @Test
    fun catalogBackslashContentUrlIsRejected() {
        assertInvalid(catalogWithContentUrl("/content/..\\packages/tahlil-v1.json"))
    }

    @Test
    fun catalogWhitespaceContentUrlIsRejected() {
        assertInvalid(catalogWithContentUrl("/content/packages/tahlil v1.json"))
    }

    @Test
    fun catalogContentUrlOutsideContentDirectoryIsRejected() {
        assertInvalid(catalogWithContentUrl("/other/packages/tahlil-v1.json"))
    }

    @Test
    fun catalogRelativeContentUrlWithoutLeadingSlashIsRejected() {
        assertInvalid(catalogWithContentUrl("content/packages/tahlil-v1.json"))
    }

    @Test
    fun catalogProductionContentUrlShapeIsAccepted() {
        // The exact shape both app/src/main/assets/content/catalog.json and
        // content-hosting/public/content/catalog.json ship — the pin must not reject real content.
        val result = ContentValidator.validateCatalog(catalogWith(contentUrl = "/content/packages/tahlil-v1.json"))

        assertTrue(result is ContentValidation.Valid)
    }

    @Test
    fun catalogHttpImageUrlIsRejected() {
        assertInvalid(catalogWithImageUrl("http://images.example/tahlil.png"))
    }

    @Test
    fun catalogDataUriImageUrlIsRejected() {
        assertInvalid(catalogWithImageUrl("data:image/png;base64,AAAA"))
    }

    @Test
    fun catalogFileImageUrlIsRejected() {
        assertInvalid(catalogWithImageUrl("file:///data/data/com.sangusantri.app/databases/sangu.db"))
    }

    @Test
    fun catalogSchemeOnlyImageUrlIsRejected() {
        assertInvalid(catalogWithImageUrl("https://"))
    }

    @Test
    fun catalogHttpsImageUrlIsAccepted() {
        val result = ContentValidator.validateCatalog(catalogWith(imageUrl = "https://images.example/tahlil.png"))

        assertTrue(result is ContentValidation.Valid)
    }

    @Test
    fun catalogAbsentImageUrlIsAccepted() {
        val result = ContentValidator.validateCatalog(catalogWith(imageUrl = null))

        assertTrue(result is ContentValidation.Valid)
    }

    private fun catalogWithContentUrl(contentUrl: String) =
        ContentValidator.validateCatalog(catalogWith(contentUrl = contentUrl))

    private fun catalogWithImageUrl(imageUrl: String) =
        ContentValidator.validateCatalog(catalogWith(imageUrl = imageUrl))

    private fun catalogWith(
        contentUrl: String = "/content/packages/tahlil-v1.json",
        imageUrl: String? = null,
    ): ContentCatalogDto {
        val catalog = validCatalog()
        return catalog.copy(items = listOf(catalog.items[0].copy(contentUrl = contentUrl, imageUrl = imageUrl)))
    }

    @Test
    fun validContentFilePassesValidation() {
        val result = ContentValidator.validateContentFile(validContentFile())

        assertTrue(result is ContentValidation.Valid)
    }

    @Test
    fun contentFileUnsupportedSchemaVersionIsRejected() {
        val result = ContentValidator.validateContentFile(validContentFile().copy(schemaVersion = 99))

        assertInvalid(result)
    }

    @Test
    fun contentFileEmptyStepsIsRejected() {
        val result = ContentValidator.validateContentFile(validContentFile().copy(steps = emptyList()))

        assertInvalid(result)
    }

    @Test
    fun contentFileDuplicateStepIdsAreRejected() {
        val file = validContentFile()
        val result = ContentValidator.validateContentFile(file.copy(steps = file.steps + file.steps[0]))

        assertInvalid(result)
    }

    @Test
    fun contentFileBlankArabicTextIsRejected() {
        val step = ContentStepDto(id = "s1", arabicText = " ", translation = "[FIXTURE]", repeatTarget = 1)
        val result = ContentValidator.validateContentFile(validContentFile().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun contentFileBlankTranslationIsRejected() {
        val step = ContentStepDto(id = "s1", arabicText = "[FIXTURE-AR]", translation = " ", repeatTarget = 1)
        val result = ContentValidator.validateContentFile(validContentFile().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun contentFileNonPositiveRepeatTargetIsRejected() {
        val step = ContentStepDto(id = "s1", arabicText = "[FIXTURE-AR]", translation = "[FIXTURE]", repeatTarget = 0)
        val result = ContentValidator.validateContentFile(validContentFile().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    private fun assertInvalid(result: ContentValidation) {
        assertTrue(result is ContentValidation.Invalid)
    }

    private fun validCatalog(): ContentCatalogDto =
        ContentCatalogDto(
            schemaVersion = ContentValidator.SUPPORTED_SCHEMA_VERSION,
            items =
                listOf(
                    ContentCatalogItemDto(
                        id = "tahlil",
                        title = "Tahlil",
                        description = "[FIXTURE] Tahlil",
                        imageUrl = null,
                        category = "Tahlil dan Doa",
                        version = 1,
                        contentUrl = "/content/packages/tahlil-v1.json",
                        order = 1,
                        isActive = true,
                    ),
                ),
        )

    private fun validContentFile(): ContentFileDto =
        ContentFileDto(
            schemaVersion = ContentValidator.SUPPORTED_SCHEMA_VERSION,
            id = "tahlil",
            version = 1,
            sourceName = "NON-PRODUCTION FIXTURE",
            sourceUrl = "https://example.invalid/fixture",
            steps =
                listOf(
                    ContentStepDto(id = "s1", arabicText = "[FIXTURE-AR]", translation = "[FIXTURE]", repeatTarget = 1),
                ),
        )
}
