package com.sangusantri.app.data.local.seed

import com.sangusantri.app.data.local.seed.dto.AmaliyahStepDto
import com.sangusantri.app.data.local.seed.dto.ContentPackageDto
import com.sangusantri.app.domain.model.StepType

sealed interface ContentPackageValidation {
    data object Valid : ContentPackageValidation

    data class Invalid(
        val reason: String,
    ) : ContentPackageValidation
}

/** Pure structural validation of a parsed content package, run before any database write (PRD 12.4). */
object SeedContentValidator {
    const val SUPPORTED_SCHEMA_VERSION = 1

    fun validate(pkg: ContentPackageDto): ContentPackageValidation {
        val reason = validateIdentifiers(pkg) ?: validateSteps(pkg)
        return reason?.let { ContentPackageValidation.Invalid(it) } ?: ContentPackageValidation.Valid
    }

    private fun validateIdentifiers(pkg: ContentPackageDto): String? =
        when {
            pkg.schemaVersion != SUPPORTED_SCHEMA_VERSION -> "unsupported schemaVersion ${pkg.schemaVersion}"
            pkg.amaliyah.id.isBlank() || pkg.amaliyah.slug.isBlank() -> "amaliyah.id/slug must not be blank"
            pkg.variant.id.isBlank() || pkg.variant.slug.isBlank() -> "variant.id/slug must not be blank"
            pkg.version.id.isBlank() -> "version.id must not be blank"
            pkg.version.versionNumber <= 0 -> "version.versionNumber must be positive"
            pkg.approval.id.isBlank() -> "approval.id must not be blank"
            else -> null
        }

    // Four sequential, independent checks that each short-circuit on failure — flat guard
    // clauses are clearer here than folding them into a single boolean/when expression.
    @Suppress("ReturnCount")
    private fun validateSteps(pkg: ContentPackageDto): String? {
        if (pkg.steps.isEmpty()) return "steps must not be empty"

        val stepIds = pkg.steps.map { it.id }
        if (stepIds.any { it.isBlank() }) return "step.id must not be blank"
        if (stepIds.distinct().size != stepIds.size) return "step.id values must be unique"

        val positions = pkg.steps.map { it.position }
        if (positions.any { it <= 0 }) return "step.position must be positive"
        if (positions.distinct().size != positions.size) return "step.position values must be unique"

        return pkg.steps.firstNotNullOfOrNull { step ->
            validateStep(step)?.let { reason -> "step ${step.id}: $reason" }
        }
    }

    private fun validateStep(step: AmaliyahStepDto): String? =
        when (step.stepType) {
            StepType.HEADING -> validateHeading(step)
            StepType.INSTRUCTION -> validateInstruction(step)
            StepType.ARABIC_TEXT -> validateArabicText(step)
            StepType.PRAYER -> validateArabicText(step)
            StepType.REPEATED_READING -> validateRepeatedReading(step)
            StepType.QURAN_AYAH -> validateQuranAyah(step)
            StepType.DIVIDER -> null
            StepType.CLOSING -> validateClosing(step)
        }

    private fun validateHeading(step: AmaliyahStepDto): String? =
        if (step.titleId.isNullOrBlank() && step.titleAr.isNullOrBlank()) {
            "HEADING requires titleId or titleAr"
        } else {
            null
        }

    private fun validateInstruction(step: AmaliyahStepDto): String? =
        if (step.instructionId.isNullOrBlank()) "INSTRUCTION requires instructionId" else null

    private fun validateArabicText(step: AmaliyahStepDto): String? =
        if (step.arabicText.isNullOrBlank()) "${step.stepType} requires arabicText" else null

    private fun validateRepeatedReading(step: AmaliyahStepDto): String? =
        when {
            step.arabicText.isNullOrBlank() -> "REPEATED_READING requires arabicText"
            step.repeatTarget == null || step.repeatTarget <= 0 -> "REPEATED_READING requires a positive repeatTarget"
            else -> null
        }

    private fun validateQuranAyah(step: AmaliyahStepDto): String? =
        when {
            step.arabicText.isNullOrBlank() -> "QURAN_AYAH requires arabicText"
            step.quranSurahNumber == null || step.quranSurahNumber <= 0 ->
                "QURAN_AYAH requires a positive quranSurahNumber"
            step.quranAyahStart == null || step.quranAyahStart <= 0 ->
                "QURAN_AYAH requires a positive quranAyahStart"
            step.quranAyahEnd != null && step.quranAyahEnd < step.quranAyahStart ->
                "QURAN_AYAH quranAyahEnd must not precede quranAyahStart"
            else -> null
        }

    private fun validateClosing(step: AmaliyahStepDto): String? =
        if (step.titleId.isNullOrBlank() && step.instructionId.isNullOrBlank()) {
            "CLOSING requires titleId or instructionId"
        } else {
            null
        }
}
