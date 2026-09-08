package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.ContentValidator.validateDetail
import com.sangusantri.app.data.content.ContentValidator.validateList
import com.sangusantri.app.data.content.dto.ContentDetailDto
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
 * Pure structural validation of parsed CMS content, run before any database write.
 *
 * One contract, `schemaVersion` 3: a metadata list per category ([validateList]) and a detail per
 * item carrying its steps ([validateDetail]).
 */
object ContentValidator {
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

    /** Item images are handed straight to Coil, so a tampered catalog must not be able to point
     * the app at an arbitrary tracker, a cleartext host, or a `file:`/`content:` URI. Absent is
     * always fine — the field is optional. */
    fun isAllowedImageUrl(imageUrl: String?): Boolean =
        imageUrl == null ||
            (
                imageUrl.startsWith(HTTPS_SCHEME) &&
                    imageUrl.length > HTTPS_SCHEME.length &&
                    imageUrl.none(Char::isWhitespace)
                )

    // Three sequential, independent checks that each short-circuit on failure — flat guard
    // clauses are clearer here than folding them into a single boolean/when expression.
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

    private const val HTTPS_SCHEME = "https://"
}
