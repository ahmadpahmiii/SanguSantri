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
import androidx.compose.ui.unit.dp
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderSettings

/**
 * Renders one ordered [ContentStep] — Arabic text, its translation, and a repetition shortcut
 * into Guided Reader (FR-018). Every step has the same shape (ADR 0015: no step "type" any
 * more — the former `HEADING`/`INSTRUCTION`/`QURAN_AYAH`/`DIVIDER`/`CLOSING` distinctions are
 * gone), so there is no per-step-kind branching here any more.
 */
@Composable
fun ReaderStepItem(
    step: ContentStep,
    settings: ReaderSettings,
    onOpenGuidedAtStep: (stepId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Revamp handoff §7: steps are plain rows separated by a hairline, not bordered cards. The
    // design's whole hierarchy comes from typography and hairlines — no elevation, no card stack.
    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = StepVerticalPadding)) {
            ReaderStepFields(step, settings) { target ->
                ReaderRepetitionShortcut(target) { onOpenGuidedAtStep(step.id) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

private val StepVerticalPadding = 22.dp

/**
 * `internal` (not `private`) so the Guided Reader (Milestone 4, `feature/guidedreader`) can reuse
 * the exact same field layout with an interactive tasbih counter swapped in for
 * [repetitionContent] instead of the Full Reader's `ReaderRepetitionShortcut` (FR-018) — one
 * canonical step layout, per `CODING_STANDARD.md`'s no-duplication rule.
 */
@Composable
internal fun ReaderStepFields(
    step: ContentStep,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
    repetitionContent: @Composable (target: Int) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        ReaderArabicBlock(step.arabicText, settings)
        if (settings.showTranslation) {
            ReaderTranslationBlock(step.translation, settings)
        }
        repetitionContent(step.repeatTarget)
    }
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewSettings = ReaderSettings()

private val previewStep =
    ContentStep(
        id = "preview-step",
        contentId = "preview-content",
        position = 1,
        arabicText = "[FIXTURE-AR] بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translation = "[FIXTURE] Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.",
        repeatTarget = 1,
    )

private val previewLongStep =
    previewStep.copy(
        id = "preview-long",
        arabicText = List(6) { previewStep.arabicText }.joinToString(separator = " "),
        translation = List(6) { previewStep.translation }.joinToString(separator = " "),
    )

private val previewRepeatedStep =
    previewStep.copy(
        id = "preview-repeated",
        arabicText = "[FIXTURE-AR] سُبْحَانَ اللَّهِ",
        translation = "[FIXTURE] Maha Suci Allah.",
        repeatTarget = 33,
    )

@PreviewLightDark
@Composable
private fun ReaderStepItemNormalPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewStep, previewSettings, onOpenGuidedAtStep = {})
            ReaderStepItem(previewRepeatedStep, previewSettings, onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Long Arabic content")
@Composable
private fun ReaderStepItemLongArabicPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewLongStep, previewSettings, onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Translation hidden")
@Composable
private fun ReaderStepItemTranslationHiddenPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(previewStep, previewSettings.copy(showTranslation = false), onOpenGuidedAtStep = {})
        }
    }
}

@Preview(name = "Large Arabic font")
@Composable
private fun ReaderStepItemLargeFontPreview() {
    SanguSantriTheme {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            ReaderStepItem(
                previewStep,
                previewSettings.copy(arabicFontSizeSp = ReaderSettings.MAX_ARABIC_FONT_SIZE_SP),
                onOpenGuidedAtStep = {},
            )
        }
    }
}
