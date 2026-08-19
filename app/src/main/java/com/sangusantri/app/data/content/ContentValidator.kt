package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentStepDto

sealed interface ContentValidation {
    data object Valid : ContentValidation

    data class Invalid(
        val reason: String,
    ) : ContentValidation
}

/** Pure structural validation of a parsed catalog or content file, run before any database write. */
object ContentValidator {
    const val SUPPORTED_SCHEMA_VERSION = 1

    fun validateCatalog(catalog: ContentCatalogDto): ContentValidation {
        val reason = validateCatalogSchema(catalog) ?: validateCatalogItems(catalog)
        return reason?.let { ContentValidation.Invalid(it) } ?: ContentValidation.Valid
    }

    private fun validateCatalogSchema(catalog: ContentCatalogDto): String? =
        if (catalog.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            "unsupported schemaVersion ${catalog.schemaVersion}"
        } else {
            null
        }

    @Suppress("ReturnCount")
    private fun validateCatalogItems(catalog: ContentCatalogDto): String? {
        val ids = catalog.items.map { it.id }
        if (ids.any { it.isBlank() }) return "catalog item id must not be blank"
        if (ids.distinct().size != ids.size) return "duplicate catalog item id"
        return catalog.items.firstNotNullOfOrNull { item ->
            when {
                item.version <= 0 -> "item ${item.id}: version must be positive"
                item.title.isBlank() -> "item ${item.id}: title must not be blank"
                item.description.isBlank() -> "item ${item.id}: description must not be blank"
                item.contentUrl.isBlank() -> "item ${item.id}: contentUrl must not be blank"
                !isOriginRelativeContentPath(item.contentUrl) ->
                    "item ${item.id}: contentUrl must be an origin-relative path under one of $CONTENT_PATH_PREFIXES"

                !isAllowedImageUrl(item.imageUrl) -> "item ${item.id}: imageUrl must be an https URL"
                else -> null
            }
        }
    }

    /**
     * Origin pinning for the one catalog field that decides where the app fetches religious content
     * from.
     *
     * [com.sangusantri.app.data.remote.api.ContentApiService.getContent] takes this value as a
     * Retrofit `@Url`, and Retrofit resolves an *absolute* `@Url` against nothing — it replaces the
     * configured base URL outright. A catalog naming `https://elsewhere.example/tahlil.json` would
     * therefore have the app import amaliyah text from an origin nobody vetted. The bundled pipeline
     * feeds the same field to `AssetManager.open` after stripping the prefix
     * ([com.sangusantri.app.data.local.content.BundledContentBootstrapper]). Restricting it here, in
     * the one validator both pipelines run before any read, is what keeps content on the deployed
     * Firebase Hosting origin and inside the bundled asset directory.
     *
     * Also rejects a protocol-relative `//host/...` (which likewise leaves the origin), any `..`
     * segment, and backslashes or whitespace that path handling downstream could normalise
     * differently.
     */
    fun isOriginRelativeContentPath(contentUrl: String): Boolean =
        CONTENT_PATH_PREFIXES.any(contentUrl::startsWith) &&
                !contentUrl.contains("//") &&
                !contentUrl.contains('\\') &&
                contentUrl.none(Char::isWhitespace) &&
                contentUrl.split('/').none { it == ".." }

    /** Catalog images are handed straight to Coil, so a tampered catalog must not be able to point
     * the app at an arbitrary tracker, a cleartext host, or a `file:`/`content:` URI. Absent is
     * always fine — the field is optional. */
    fun isAllowedImageUrl(imageUrl: String?): Boolean =
        imageUrl == null ||
                (
                        imageUrl.startsWith(HTTPS_SCHEME) &&
                                imageUrl.length > HTTPS_SCHEME.length &&
                                imageUrl.none(Char::isWhitespace)
                        )

    fun validateContentFile(file: ContentFileDto): ContentValidation {
        val reason = validateFileIdentifiers(file) ?: validateSteps(file)
        return reason?.let { ContentValidation.Invalid(it) } ?: ContentValidation.Valid
    }

    private fun validateFileIdentifiers(file: ContentFileDto): String? =
        when {
            file.schemaVersion != SUPPORTED_SCHEMA_VERSION -> "unsupported schemaVersion ${file.schemaVersion}"
            file.id.isBlank() -> "id must not be blank"
            file.version <= 0 -> "version must be positive"
            file.sourceName.isBlank() -> "sourceName must not be blank"
            file.sourceUrl.isBlank() -> "sourceUrl must not be blank"
            else -> null
        }

    // Three sequential, independent checks that each short-circuit on failure — flat guard
    // clauses are clearer here than folding them into a single boolean/when expression.
    @Suppress("ReturnCount")
    private fun validateSteps(file: ContentFileDto): String? {
        if (file.steps.isEmpty()) return "steps must not be empty"

        val stepIds = file.steps.map { it.id }
        if (stepIds.any { it.isBlank() }) return "step.id must not be blank"
        if (stepIds.distinct().size != stepIds.size) return "step.id values must be unique"

        return file.steps.firstNotNullOfOrNull { step ->
            validateStep(step)?.let { reason -> "step ${step.id}: $reason" }
        }
    }

    private fun validateStep(step: ContentStepDto): String? =
        when {
            step.arabicText.isBlank() -> "arabicText must not be blank"
            step.translation.isBlank() -> "translation must not be blank"
            // null is legitimate — it means "no counter". Only a present-but-nonsensical
            // value is a content error.
            step.repeatTarget != null && step.repeatTarget < 1 -> "repeatTarget must be at least 1 when present"
            else -> null
        }

    /**
     * The two shapes a legitimate `contentUrl` takes. `/content/` is the bundled asset directory
     * (`app/src/main/assets/content/packages/...`); `/api/v1/content/` is the CMS API's own route
     * (`cms/api`, deployed on Vercel), which emits this path relative rather than absolute for
     * exactly the reason documented above. Both stay on the configured origin.
     */
    private val CONTENT_PATH_PREFIXES = listOf("/content/", "/api/v1/content/")
    private const val HTTPS_SCHEME = "https://"
}
