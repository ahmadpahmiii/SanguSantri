package com.sangusantri.app.data.content

import com.sangusantri.app.data.content.dto.AmaliyahDto
import com.sangusantri.app.data.content.dto.AmaliyahStepDto
import com.sangusantri.app.data.content.dto.AmaliyahVariantDto
import com.sangusantri.app.data.content.dto.AmaliyahVersionDto
import com.sangusantri.app.data.content.dto.ApprovalDto
import com.sangusantri.app.data.content.dto.ContentPackageDto
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPackageValidatorTest {
    @Test
    fun validPackagePassesValidation() {
        val result = ContentPackageValidator.validate(validPackage())

        assertTrue(result is ContentPackageValidation.Valid)
    }

    @Test
    fun unsupportedSchemaVersionIsRejected() {
        val result = ContentPackageValidator.validate(validPackage().copy(schemaVersion = 99))

        assertInvalid(result)
    }

    @Test
    fun blankAmaliyahIdIsRejected() {
        val pkg = validPackage()
        val result = ContentPackageValidator.validate(pkg.copy(amaliyah = pkg.amaliyah.copy(id = " ")))

        assertInvalid(result)
    }

    @Test
    fun emptyStepsIsRejected() {
        val result = ContentPackageValidator.validate(validPackage().copy(steps = emptyList()))

        assertInvalid(result)
    }

    @Test
    fun duplicateStepPositionsAreRejected() {
        val pkg = validPackage()
        val duplicated = pkg.steps[0].copy(id = "step-1-duplicate")
        val result = ContentPackageValidator.validate(pkg.copy(steps = pkg.steps + duplicated))

        assertInvalid(result)
    }

    @Test
    fun duplicateStepIdsAreRejected() {
        val pkg = validPackage()
        val duplicated = pkg.steps[0].copy(position = pkg.steps.size + 1)
        val result = ContentPackageValidator.validate(pkg.copy(steps = pkg.steps + duplicated))

        assertInvalid(result)
    }

    @Test
    fun arabicTextStepWithoutArabicTextIsRejected() {
        val step =
            AmaliyahStepDto(id = "s1", position = 1, stepType = StepType.ARABIC_TEXT, arabicText = null)
        val result = ContentPackageValidator.validate(validPackage().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun repeatedReadingWithoutPositiveRepeatTargetIsRejected() {
        val step =
            AmaliyahStepDto(
                id = "s1",
                position = 1,
                stepType = StepType.REPEATED_READING,
                arabicText = "[FIXTURE]",
                repeatTarget = 0,
            )
        val result = ContentPackageValidator.validate(validPackage().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun quranAyahWithoutSurahNumberIsRejected() {
        val step =
            AmaliyahStepDto(
                id = "s1",
                position = 1,
                stepType = StepType.QURAN_AYAH,
                arabicText = "[FIXTURE]",
                quranSurahNumber = null,
                quranAyahStart = 1,
            )
        val result = ContentPackageValidator.validate(validPackage().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun quranAyahEndBeforeStartIsRejected() {
        val step =
            AmaliyahStepDto(
                id = "s1",
                position = 1,
                stepType = StepType.QURAN_AYAH,
                arabicText = "[FIXTURE]",
                quranSurahNumber = 1,
                quranAyahStart = 5,
                quranAyahEnd = 2,
            )
        val result = ContentPackageValidator.validate(validPackage().copy(steps = listOf(step)))

        assertInvalid(result)
    }

    @Test
    fun dividerStepNeedsNoContent() {
        val step = AmaliyahStepDto(id = "s1", position = 1, stepType = StepType.DIVIDER)
        val result = ContentPackageValidator.validate(validPackage().copy(steps = listOf(step)))

        assertTrue(result is ContentPackageValidation.Valid)
    }

    private fun assertInvalid(result: ContentPackageValidation) {
        assertTrue(result is ContentPackageValidation.Invalid)
    }

    private fun validPackage(): ContentPackageDto =
        ContentPackageDto(
            schemaVersion = ContentPackageValidator.SUPPORTED_SCHEMA_VERSION,
            amaliyah =
                AmaliyahDto(
                    id = "tahlil",
                    slug = "tahlil",
                    titleId = "Tahlil",
                    titleAr = "[FIXTURE-AR]",
                    category = "AMALIYAH",
                ),
            variant =
                AmaliyahVariantDto(
                    id = "tahlil-umum",
                    slug = "umum",
                    nameId = "Umum",
                    nameAr = "[FIXTURE-AR]",
                    ownerType = OwnerType.PUBLIC,
                    visibility = Visibility.PUBLIC,
                    isDefault = true,
                ),
            version =
                AmaliyahVersionDto(
                    id = "tahlil-umum-v1",
                    versionNumber = 1,
                    status = AmaliyahVersionStatus.DRAFT,
                    sourceName = "NON-PRODUCTION FIXTURE",
                    sourceReference = "N/A",
                    minimumAppVersionCode = 1,
                ),
            approval =
                ApprovalDto(
                    id = "tahlil-umum-v1-approval",
                    approverName = "NON-PRODUCTION FIXTURE",
                    approverRole = "N/A",
                    approvalDate = "2026-07-25",
                    approvalScope = "N/A",
                    status = ApprovalStatus.PENDING,
                ),
            steps =
                listOf(
                    AmaliyahStepDto(id = "s1", position = 1, stepType = StepType.HEADING, titleId = "Pembukaan"),
                ),
        )
}
