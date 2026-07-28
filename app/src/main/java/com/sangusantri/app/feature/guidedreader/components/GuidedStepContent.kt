package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.feature.reader.components.ReaderDividerRow
import com.sangusantri.app.feature.reader.components.ReaderStepFields

/**
 * Renders the single visible [step], reusing [ReaderStepFields] — the exact same field-presence
 * layout the Full Reader uses (`docs/engineering/CONTENT_MODEL.md`: one canonical model, never
 * forked per reader mode) — with an interactive [GuidedTasbihCounter] swapped in wherever the Full
 * Reader would show its `ReaderRepetitionShortcut` (tap-to-switch-mode action, FR-018).
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

/** Step title + prominent repeat target (decision E) — shown above the reading card, not only inside it. */
@Composable
internal fun GuidedStepStatusRow(step: AmaliyahStep) {
    val title = step.titleId
    val target = step.repeatTarget
    if (title.isNullOrBlank() && (target == null || target <= 0)) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (target != null && target > 0) {
            Text(
                text = stringResource(R.string.guided_reader_step_target_label, target),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
