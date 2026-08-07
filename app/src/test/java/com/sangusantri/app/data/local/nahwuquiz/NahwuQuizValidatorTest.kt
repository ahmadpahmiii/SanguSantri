package com.sangusantri.app.data.local.nahwuquiz

import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizBankDto
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizPackageDto
import com.sangusantri.app.data.local.nahwuquiz.dto.NahwuQuizQuestionDto
import org.junit.Assert.assertTrue
import org.junit.Test

class NahwuQuizValidatorTest {
    @Test
    fun validBankPassesValidation() {
        val result = NahwuQuizValidator.validate(validBank())

        assertTrue(result is NahwuQuizValidation.Valid)
    }

    @Test
    fun unsupportedSchemaVersionIsRejected() {
        val result = NahwuQuizValidator.validate(validBank().copy(schemaVersion = 99))

        assertInvalid(result)
    }

    @Test
    fun blankPackageIdIsRejected() {
        val bank = validBank()
        val result = NahwuQuizValidator.validate(bank.copy(packages = listOf(bank.packages[0].copy(id = " "))))

        assertInvalid(result)
    }

    @Test
    fun duplicatePackageIdsAreRejected() {
        val bank = validBank()
        val result = NahwuQuizValidator.validate(bank.copy(packages = bank.packages + bank.packages[0]))

        assertInvalid(result)
    }

    @Test
    fun blankPackageTitleIsRejected() {
        val bank = validBank()
        val result = NahwuQuizValidator.validate(bank.copy(packages = listOf(bank.packages[0].copy(title = " "))))

        assertInvalid(result)
    }

    @Test
    fun emptyQuestionsListIsValid() {
        // A package genuinely awaiting content (design spec state 13, "Bank Soal Kosong") is
        // structurally valid — it is a display-state concern, not a validation failure.
        val bank = validBank()
        val result =
            NahwuQuizValidator.validate(
                bank.copy(packages = listOf(bank.packages[0].copy(questions = emptyList()))),
            )

        assertTrue(result is NahwuQuizValidation.Valid)
    }

    @Test
    fun duplicateQuestionIdsAreRejected() {
        val bank = validBank()
        val questions = bank.packages[0].questions
        val result =
            NahwuQuizValidator.validate(
                bank.copy(packages = listOf(bank.packages[0].copy(questions = questions + questions[0]))),
            )

        assertInvalid(result)
    }

    @Test
    fun blankStemIsRejected() {
        val result = NahwuQuizValidator.validate(bankWithSingleQuestion(validQuestion().copy(stem = " ")))

        assertInvalid(result)
    }

    @Test
    fun blankOptionIsRejected() {
        val result = NahwuQuizValidator.validate(bankWithSingleQuestion(validQuestion().copy(optionC = " ")))

        assertInvalid(result)
    }

    @Test
    fun invalidCorrectOptionKeyIsRejected() {
        val result = NahwuQuizValidator.validate(bankWithSingleQuestion(validQuestion().copy(correctOption = "E")))

        assertInvalid(result)
    }

    private fun assertInvalid(result: NahwuQuizValidation) {
        assertTrue(result is NahwuQuizValidation.Invalid)
    }

    private fun bankWithSingleQuestion(question: NahwuQuizQuestionDto): NahwuQuizBankDto {
        val bank = validBank()
        return bank.copy(packages = listOf(bank.packages[0].copy(questions = listOf(question))))
    }

    private fun validQuestion(): NahwuQuizQuestionDto =
        NahwuQuizQuestionDto(
            id = "q1",
            stem = "[FIXTURE] Pertanyaan contoh.",
            optionA = "[FIXTURE] A",
            optionB = "[FIXTURE] B",
            optionC = "[FIXTURE] C",
            optionD = "[FIXTURE] D",
            correctOption = "B",
            explanation = null,
        )

    private fun validBank(): NahwuQuizBankDto =
        NahwuQuizBankDto(
            schemaVersion = NahwuQuizValidator.SUPPORTED_SCHEMA_VERSION,
            packages =
                listOf(
                    NahwuQuizPackageDto(
                        id = "nahwu-dasar-fixture",
                        title = "[FIXTURE] Nahwu Dasar",
                        description = "[FIXTURE] Paket contoh.",
                        order = 1,
                        isActive = true,
                        questions = listOf(validQuestion()),
                    ),
                ),
        )
}
