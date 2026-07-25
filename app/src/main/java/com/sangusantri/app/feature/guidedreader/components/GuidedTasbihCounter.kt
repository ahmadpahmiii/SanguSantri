package com.sangusantri.app.feature.guidedreader.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

private val MinTouchTarget = 48.dp

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
    val progressText = stringResource(R.string.guided_counter_progress, currentCount, target)
    val tapLabel = stringResource(R.string.guided_counter_tap_action)
    val stateText =
        if (isComplete) {
            stringResource(R.string.guided_counter_completed_description, currentCount, target)
        } else {
            progressText
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        GuidedTasbihBadge(
            isComplete = isComplete,
            progressText = progressText,
            tapLabel = tapLabel,
            stateText = stateText,
            onTap = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onIncrement()
            },
        )
        TextButton(
            onClick = onRequestReset,
            modifier = Modifier.heightIn(min = MinTouchTarget),
            contentPadding = PaddingValues(horizontal = SanguSantriSpacing.default),
        ) {
            Text(text = stringResource(R.string.guided_counter_reset_action))
        }
    }
}

@Composable
private fun GuidedTasbihBadge(
    isComplete: Boolean,
    progressText: String,
    tapLabel: String,
    stateText: String,
    onTap: () -> Unit,
) {
    Surface(
        onClick = { if (!isComplete) onTap() },
        shape = SanguSantriShapes.medium,
        color = if (isComplete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outline),
        modifier =
            Modifier
                .sizeIn(minWidth = MinTouchTarget * 2, minHeight = MinTouchTarget)
                .semantics {
                    contentDescription = tapLabel
                    stateDescription = stateText
                },
    ) {
        Box(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color =
                        if (isComplete) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
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
