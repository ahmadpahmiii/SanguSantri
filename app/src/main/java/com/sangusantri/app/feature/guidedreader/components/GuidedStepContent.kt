package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
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
        else -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SanguSantriDimensions.guidedCardCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outline),
            ) {
                ReaderStepFields(
                    step = step,
                    settings = settings,
                    isClosing = step.stepType == StepType.CLOSING,
                    modifier =
                        Modifier.padding(
                            horizontal = SanguSantriDimensions.readerHorizontalPadding,
                            vertical = SanguSantriDimensions.readerCardVerticalPadding,
                        ),
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
    }
}

/** Step title + prominent repeat target (decision E) — shown above the reading card, not only inside it. */
@Composable
internal fun GuidedStepStatusRow(
    step: AmaliyahStep,
    sectionTitle: String?,
) {
    val title = step.titleId?.takeIf { it.isNotBlank() } ?: sectionTitle
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
                modifier = Modifier.weight(1f),
            )
        }
        if (target != null && target > 0) {
            Text(
                text = stringResource(R.string.guided_reader_step_target_label, target),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = SanguSantriSpacing.small),
            )
        }
    }
}

internal fun List<AmaliyahStep>.currentSectionTitle(stepIndex: Int): String? =
    take(stepIndex + 1)
        .lastOrNull { it.stepType == StepType.HEADING && !it.titleId.isNullOrBlank() }
        ?.titleId
