package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.feature.reader.components.ReaderDividerRow
import com.sangusantri.app.feature.reader.components.ReaderStepFields

/**
 * Renders the single visible [step], reusing [ReaderStepFields] — the exact same field-presence
 * layout the Full Reader uses (`docs/engineering/CONTENT_MODEL.md`: one canonical model, never
 * forked per reader mode) — with an interactive [GuidedTasbihCounter] swapped in wherever the Full
 * Reader would show its informational `ReaderRepetitionIndicator`.
 */
@Composable
internal fun GuidedStepContent(
    step: AmaliyahStep,
    settings: ReaderSettings,
    currentCount: Int,
    actions: TasbihActions,
    modifier: Modifier = Modifier,
) {
    when (step.stepType) {
        StepType.DIVIDER -> ReaderDividerRow(modifier)
        else ->
            ReaderStepFields(
                step = step,
                settings = settings,
                isClosing = step.stepType == StepType.CLOSING,
                modifier = modifier,
            ) { target ->
                GuidedTasbihCounter(
                    currentCount = currentCount,
                    target = target,
                    onIncrement = actions.onIncrement,
                    onRequestReset = actions.onRequestReset,
                )
            }
    }
}
