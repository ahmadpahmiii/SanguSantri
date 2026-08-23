package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import com.sangusantri.app.data.content.dto.ContentStepDto

sealed interface ContentValidation {
    data object Valid : ContentValidation

    data class Invalid(
        val reason: String,
    ) : ContentValidation
}

/**
 * Pure structural validation of parsed content, run before any database write.
 *
 * Two contracts live here because there are two sources. The bundled assets
 * ([validateCatalog]/[validateContentFile]) are `schemaVersion` 1: a catalog of metadata plus
 * separate package files, joined by `contentUrl`. The CMS API ([validateList]/[validateDetail]) is
 * `schemaVersion` 3: a metadata list per category, and a detail per item carrying its steps.
 */
@Suppress("TooManyFunctions")
object ContentValidator {
    /** Bundled assets (`assets/content/catalog.json` and its packages). */
    const val SUPPORTED_SCHEMA_VERSION = 1

    /** The CMS API's list and detail routes. */
    const val SUPPORTED_REMOTE_SCHEMA_VERSION = 3

    /**
     * Rejecting the whole list on one bad entry is deliberate: a half-imported catalogue leaves
     * Beranda showing an arbitrary subset with nothing to say which items are missing. The caller
     * reports this as a permanent failure and keeps whatever Room already holds.
     */
    fun validateList(response: ContentListResponseDto): ContentValidation {
        val reason =
            validateRemoteSchema(response.schemaVersion) ?: validateListItems(response)
        return reason?.let { ContentValidation.Invalid(it) } ?: ContentValidation.Valid
    }

    /**
     * A detail is validated on its own, and a bad one fails only that item — unlike the list, one
     * unreadable item does not have to cost the reader the other sixty-two.
     */
    fun validateDetail(detail: ContentDetailDto): ContentValidation {
        val reason =
            validateRemoteSchema(detail.schemaVersion)
                ?: validateDetailIdentifiers(detail)
                ?: validateStepList(detail.steps)
        return reason?.let { ContentValidation.Invalid(it) } ?: ContentValidation.Valid
    }

    private fun validateRemoteSchema(schemaVersion: Int): String? =
        if (schemaVersion != SUPPORTED_REMOTE_SCHEMA_VERSION) {
            "unsupported schemaVersion $schemaVersion"
        } else {
            null
        }

    @Suppress("ReturnCount")
    private fun validateListItems(response: ContentListResponseDto): String? {
        val ids = response.items.map { it.id }
        if (ids.any { it.isBlank() }) return "item id must not be blank"
        if (ids.distinct().size != ids.size) return "duplicate item id"
        return response.items.firstNotNullOfOrNull(::listItemReason)
    }

    /** One list entry on its own, for the importer's own last-line guard. */
    fun validateListItem(item: ContentListItemDto): ContentValidation =
        listItemReason(item)?.let { ContentValidation.Invalid(it) } ?: ContentValidation.Valid

    private fun listItemReason(item: ContentListItemDto): String? =
        when {
            item.title.isBlank() -> "item ${item.id}: title must not be blank"
            item.description.isBlank() -> "item ${item.id}: description must not be blank"
            !isAllowedImageUrl(item.imageUrl) -> "item ${item.id}: imageUrl must be an https URL"
            else -> null
        }

    private fun validateDetailIdentifiers(detail: ContentDetailDto): String? =
        when {
            detail.id.isBlank() -> "id must not be blank"
            detail.title.isBlank() -> "title must not be blank"
            // Source attribution is required, not decorative: religious text a reader cannot trace
            // has no business in the reader (CONTENT_GOVERNANCE.md).
            detail.sourceName.isBlank() -> "sourceName must not be blank"
            detail.sourceUrl.isBlank() -> "sourceUrl must not be blank"
            !isAllowedImageUrl(detail.imageUrl) -> "imageUrl must be an https URL"
            else -> null
        }

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
     * This used to guard a Retrofit `@Url`: the remote catalog named the URL each content file was
     * fetched from, and Retrofit resolves an *absolute* `@Url` by replacing the configured base URL
     * outright, so a catalog naming `https://elsewhere.example/tahlil.json` would have had the app
     * import amaliyah text from an origin nobody vetted. `schemaVersion` 2 removed that field, and
     * with it the remote half of this risk.
     *
     * It still guards the bundled pipeline, which feeds the same field to `AssetManager.open` after
     * stripping the prefix ([com.sangusantri.app.data.local.content.BundledContentBootstrapper]) —
     * so a `..` segment here would read outside the bundled asset directory.
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

    private fun validateSteps(file: ContentFileDto): String? = validateStepList(file.steps)

    // Three sequential, independent checks that each short-circuit on failure — flat guard
    // clauses are clearer here than folding them into a single boolean/when expression. Shared by
    // both contracts: a step is a step whether it arrived inlined or in a package file.
    @Suppress("ReturnCount")
    private fun validateStepList(steps: List<ContentStepDto>): String? {
        if (steps.isEmpty()) return "steps must not be empty"

        val stepIds = steps.map { it.id }
        if (stepIds.any { it.isBlank() }) return "step.id must not be blank"
        if (stepIds.distinct().size != stepIds.size) return "step.id values must be unique"

        return steps.firstNotNullOfOrNull { step ->
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
     * The one shape a legitimate `contentUrl` takes: `/content/`, the bundled asset directory
     * (`app/src/main/assets/content/packages/...`). The CMS once served a second prefix here; it
     * was removed with the field itself, because no remote payload carries a URL any more — a
     * `schemaVersion` 3 detail is addressed by id, not by a URL the server hands over.
     */
    private val CONTENT_PATH_PREFIXES = listOf("/content/")
    private const val HTTPS_SCHEME = "https://"
}
