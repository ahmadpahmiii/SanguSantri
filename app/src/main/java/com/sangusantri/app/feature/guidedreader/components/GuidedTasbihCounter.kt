package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * The Guided Reader's interactive tasbih (FR-006): tapping increments the count up to [target],
 * never past it; each valid tap triggers haptic feedback. Completion is signalled with both a
 * check icon and a colour change (`ACCESSIBILITY.md`: colour must never be the only status
 * signal), and exposed to accessibility services via [stateDescription] rather than just the
 * visible text.
 */
@Composable
internal fun GuidedTasbihCounter(
    currentCount: Int,
    target: Int,
    onIncrement: () -> Unit,
    onRequestReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val isComplete = currentCount >= target
    val targetText = stringResource(R.string.guided_counter_target, target)
    val tapLabel = stringResource(R.string.guided_counter_tap_action)
    val stateText =
        if (isComplete) {
            stringResource(R.string.guided_counter_completed_description, currentCount, target)
        } else {
            stringResource(R.string.guided_counter_progress_description, currentCount, target)
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        GuidedTasbihBadge(
            visualState =
                GuidedCounterVisualState(
                    isComplete = isComplete,
                    currentCount = currentCount,
                    targetText = targetText,
                    tapLabel = tapLabel,
                    stateText = stateText,
                ),
            onTap = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onIncrement()
            },
        )
        TextButton(
            onClick = onRequestReset,
            modifier = Modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget),
            contentPadding = PaddingValues(horizontal = SanguSantriSpacing.default),
        ) {
            Text(text = stringResource(R.string.guided_counter_reset_action))
        }
    }
}

private data class GuidedCounterVisualState(
    val isComplete: Boolean,
    val currentCount: Int,
    val targetText: String,
    val tapLabel: String,
    val stateText: String,
)

@Composable
private fun GuidedTasbihBadge(
    visualState: GuidedCounterVisualState,
    onTap: () -> Unit,
) {
    Surface(
        onClick = { if (!visualState.isComplete) onTap() },
        shape = SanguSantriShapes.extraLarge,
        color =
            if (visualState.isComplete) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        modifier =
            Modifier
                .width(SanguSantriDimensions.guidedCounterWidth)
                .heightIn(min = SanguSantriDimensions.guidedCounterHeight)
                .semantics {
                    contentDescription = visualState.tapLabel
                    stateDescription = visualState.stateText
                },
    ) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (visualState.isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = visualState.currentCount.toString(),
                fontSize = 58.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                    if (visualState.isComplete) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
            )
            Text(
                text = visualState.targetText,
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (visualState.isComplete) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GuidedTasbihCounterInProgressPreview() {
    SanguSantriTheme {
        GuidedTasbihCounter(currentCount = 12, target = 33, onIncrement = {}, onRequestReset = {})
    }
}

@PreviewLightDark
@Composable
private fun GuidedTasbihCounterCompletePreview() {
    SanguSantriTheme {
        GuidedTasbihCounter(currentCount = 33, target = 33, onIncrement = {}, onRequestReset = {})
    }
}
