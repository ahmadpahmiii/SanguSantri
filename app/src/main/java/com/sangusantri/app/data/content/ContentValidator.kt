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
                else -> null
            }
        }
    }

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
            step.repeatTarget < 1 -> "repeatTarget must be at least 1"
            else -> null
        }
}
