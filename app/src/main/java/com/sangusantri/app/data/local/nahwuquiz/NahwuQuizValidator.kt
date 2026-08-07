package com.sangusantri.app.data.local.nahwuquiz

import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizBankDto
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizPackageDto
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizQuestionDto

sealed interface NahwuQuizValidation {
    data object Valid : NahwuQuizValidation

    data class Invalid(
        val reason: String,
    ) : NahwuQuizValidation
}

/** Pure structural validation of a parsed bundled question bank, run before any database write —
 * mirrors `ContentValidator`'s shape. */
object NahwuQuizValidator {
    const val SUPPORTED_SCHEMA_VERSION = 1
    private val VALID_OPTION_KEYS = setOf("A", "B", "C", "D")

    fun validate(bank: NahwuQuizBankDto): NahwuQuizValidation {
        val reason = validateSchema(bank) ?: validatePackages(bank)
        return reason?.let { NahwuQuizValidation.Invalid(it) } ?: NahwuQuizValidation.Valid
    }

    private fun validateSchema(bank: NahwuQuizBankDto): String? =
        if (bank.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            "unsupported schemaVersion ${bank.schemaVersion}"
        } else {
            null
        }

    @Suppress("ReturnCount")
    private fun validatePackages(bank: NahwuQuizBankDto): String? {
        val ids = bank.packages.map { it.id }
        if (ids.any { it.isBlank() }) return "package id must not be blank"
        if (ids.distinct().size != ids.size) return "duplicate package id"
        return bank.packages.firstNotNullOfOrNull { pkg ->
            validatePackage(pkg)?.let { reason -> "package ${pkg.id}: $reason" }
        }
    }

    @Suppress("ReturnCount")
    private fun validatePackage(pkg: NahwuQuizPackageDto): String? {
        if (pkg.title.isBlank()) return "title must not be blank"
        if (pkg.description.isBlank()) return "description must not be blank"
        val questionIds = pkg.questions.map { it.id }
        if (questionIds.any { it.isBlank() }) return "question id must not be blank"
        if (questionIds.distinct().size != questionIds.size) return "duplicate question id"
        return pkg.questions.firstNotNullOfOrNull { question ->
            validateQuestion(question)?.let { reason -> "question ${question.id}: $reason" }
        }
    }

    @Suppress("ReturnCount")
    private fun validateQuestion(question: NahwuQuizQuestionDto): String? =
        when {
            question.stem.isBlank() -> "stem must not be blank"
            question.optionA.isBlank() -> "optionA must not be blank"
            question.optionB.isBlank() -> "optionB must not be blank"
            question.optionC.isBlank() -> "optionC must not be blank"
            question.optionD.isBlank() -> "optionD must not be blank"
            question.correctOption !in VALID_OPTION_KEYS -> "correctOption must be one of A/B/C/D"
            else -> null
        }
}
