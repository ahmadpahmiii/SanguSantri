package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.StepType

/**
 * Renders one ordered [AmaliyahStep] by which fields are present, not by a per-amaliyah layout
 * (Milestone 3: "must support the currently defined content step types without hardcoding
 * separate layouts specifically for Tahlil or Istighosah"). [StepType] is a closed enum handled
 * exhaustively below, so an unrecognised step type is a compile error, not a runtime fallback.
 */
@Composable
fun ReaderStepItem(
    step: AmaliyahStep,
    settings: ReaderSettings,
    onOpenGuidedAtStep: (stepId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (step.stepType) {
        StepType.DIVIDER -> ReaderDividerRow(modifier)
        StepType.CLOSING ->
            ReaderStepFields(step, settings, isClosing = true, modifier) { target ->
                ReaderRepetitionShortcut(target) { onOpenGuidedAtStep(step.id) }
            }
        StepType.HEADING,
        StepType.INSTRUCTION,
        StepType.ARABIC_TEXT,
        StepType.QURAN_AYAH,
        StepType.PRAYER,
        StepType.REPEATED_READING,
            ->
            ReaderStepFields(step, settings, isClosing = false, modifier) { target ->
                ReaderRepetitionShortcut(target) { onOpenGuidedAtStep(step.id) }
            }
    }
}

/**
 * `internal` (not `private`) so the Guided Reader (Milestone 4, `feature/guidedreader`) can reuse
 * the exact same field-presence rendering with an interactive tasbih counter swapped in for
 * [repetitionContent] instead of the Full Reader's `ReaderRepetitionShortcut` (FR-018) — one
 * canonical step layout, per `CODING_STANDARD.md`'s no-duplication rule.
 */
@Composable
internal fun ReaderStepFields(
    step: AmaliyahStep,
    settings: ReaderSettings,
    isClosing: Boolean,
    modifier: Modifier = Modifier,
    repetitionContent: @Composable (target: Int) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    top = if (isClosing) SanguSantriSpacing.large else SanguSantriSpacing.small,
                    bottom = SanguSantriSpacing.small,
                ),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        if (isClosing) ReaderClosingDivider()

        step.titleId?.takeIf { it.isNotBlank() }?.let { ReaderHeadingText(it, isClosing) }
        step.titleAr?.takeIf { it.isNotBlank() }?.let { ReaderHeadingArabicText(it, settings) }

        if (!step.instructionId.isNullOrBlank() || !step.instructionAr.isNullOrBlank()) {
            ReaderInstructionText(step.instructionId, step.instructionAr)
        }

        step.arabicText?.takeIf { it.isNotBlank() }?.let { arabicText ->
            val surah = step.quranSurahNumber
            val ayahStart = step.quranAyahStart
            if (surah != null && ayahStart != null) {
                ReaderQuranReference(surah, ayahStart, step.quranAyahEnd)
            }

            ReaderArabicBlock(arabicText, settings)

            val translation = step.translationId
            if (settings.showTranslation && !translation.isNullOrBlank()) {
                ReaderTranslationBlock(translation, settings)
            }

            val repeatTarget = step.repeatTarget
            if (repeatTarget != null && repeatTarget > 0) {
                repetitionContent(repeatTarget)
            }
        }
    }
}

/** `internal` — reused by the Guided Reader for its own `DIVIDER` steps. */
@Composable
internal fun ReaderDividerRow(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = SanguSantriSpacing.default),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun ReaderClosingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(bottom = SanguSantriSpacing.small),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewSettings = ReaderSettings()

private val previewArabicStep =
    AmaliyahStep(
        id = "preview-arabic",
        versionId = "preview-version",
        position = 1,
        stepType = StepType.ARABIC_TEXT,
        titleId = null,
        titleAr = null,
        arabicText = "[FIXTURE-AR] بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translationId = "[FIXTURE] Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
        instructionId = null,
        instructionAr = null,
        repeatTarget = null,
        quranSurahNumber = null,
        quranAyahStart = null,
        quranAyahEnd = null,
        audioGroupId = null,
    )

private val previewLongArabicStep =
    previewArabicStep.copy(
        id = "preview-long-arabic",
        arabicText = List(6) { previewArabicStep.arabicText }.joinToString(separator = " "),
        translationId = List(6) { previewArabicStep.translationId }.joinToString(separator = " "),
    )

private val previewQuranStep =
    previewArabicStep.copy(
        id = "preview-quran",
        stepType = StepType.QURAN_AYAH,
        quranSurahNumber = 1,
        quranAyahStart = 1,
        quranAyahEnd = 1,
    )

private val previewRepeatedStep =
    previewArabicStep.copy(
        id = "preview-repeated",
        stepType = StepType.REPEATED_READING,
        arabicText = "[FIXTURE-AR] سُبْحَانَ اللَّهِ",
        translationId = "[FIXTURE] Maha Suci Allah.",
        repeatTarget = 33,
    )

private val previewHeadingStep =
    AmaliyahStep(
        id = "preview-heading",
        versionId = "preview-version",
        position = 0,
        stepType = StepType.HEADING,
        titleId = "Pembukaan",
        titleAr = "[FIXTURE-AR] اَلْفَاتِحَة",
        arabicText = null,
        translationId = null,
        instructionId = null,
        instructionAr = null,
        repeatTarget = null,
        quranSurahNumber = null,
        quranAyahStart = null,
        quranAyahEnd = null,
        audioGroupId = null,
    )

private val previewInstructionStep =
    AmaliyahStep(
        id = "preview-instruction",
        versionId = "preview-version",
        position = 0,
        stepType = StepType.INSTRUCTION,
        titleId = null,
        titleAr = null,
        arabicText = null,
        translationId = null,
        instructionId = "[FIXTURE] Dibaca dengan suara pelan.",
        instructionAr = null,
        repeatTarget = null,
        quranSurahNumber = null,
        quranAyahStart = null,
        quranAyahEnd = null,
        audioGroupId = null,
    )

@PreviewLightDark
@Composable
private fun ReaderStepItemNormalPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewHeadingStep, previewSettings, onOpenGuidedAtStep = {})
            ReaderStepItem(previewInstructionStep, previewSettings, onOpenGuidedAtStep = {})
            ReaderStepItem(previewQuranStep, previewSettings, onOpenGuidedAtStep = {})
            ReaderStepItem(previewRepeatedStep, previewSettings, onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Long Arabic content")
@Composable
private fun ReaderStepItemLongArabicPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewLongArabicStep, previewSettings, onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Translation hidden")
@Composable
private fun ReaderStepItemTranslationHiddenPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewArabicStep, previewSettings.copy(showTranslation = false), onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Large Arabic font")
@Composable
private fun ReaderStepItemLargeFontPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(
                previewArabicStep,
                previewSettings.copy(arabicFontSizeSp = ReaderSettings.MAX_ARABIC_FONT_SIZE_SP),
                onOpenGuidedAtStep = {},
            )
        }
    }
}
