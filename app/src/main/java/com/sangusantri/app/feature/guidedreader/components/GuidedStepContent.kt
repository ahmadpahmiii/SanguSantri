package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.foundation.BorderStroke
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
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.reader.components.ReaderStepFields

/**
 * Renders the single visible [step], reusing [ReaderStepFields] — the exact same layout the Full
 * Reader uses (ADR 0015: one canonical step model, never forked per reader mode) — with an
 * interactive [GuidedTasbihCounter] swapped in wherever the Full Reader would show its
 * `ReaderRepetitionShortcut` (tap-to-switch-mode action, FR-018).
 */
@Composable
internal fun GuidedStepContent(
    step: ContentStep,
    settings: ReaderSettings,
    currentCount: Int,
    actions: TasbihActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SanguSantriDimensions.guidedCardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outline),
    ) {
        ReaderStepFields(
            step = step,
            settings = settings,
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

/**
 * Prominent repeat target (decision E) — shown above the reading card, not only inside it.
 * Renders nothing for a step with no target; there is no count to state.
 */
@Composable
internal fun GuidedStepStatusRow(step: ContentStep) {
    val target = step.effectiveRepeatTarget ?: return
    Text(
        text = stringResource(R.string.guided_reader_step_target_label, target),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}
