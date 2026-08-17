package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

@Suppress("LongParameterList")
/**
 * The Standalone Tasbih counter (0.0.2) — the single largest tappable element on the screen
 * (`docs/design/DESIGN_SYSTEM.md`'s Tasbih target hierarchy), min 220dp. Unlike
 * [com.sangusantri.app.feature.guidedreader.components.GuidedTasbihCounter] (which disables
 * tapping once its step target is reached), tapping here while [TasbihCounterTone.TARGET_REACHED]
 * is the documented "ketuk untuk mengulang" (tap to repeat) interaction that starts a new counting
 * cycle — a deliberate difference, not an inconsistency, so [onTap] is never guarded here.
 */
@Composable
fun TasbihCounter(
    count: Int,
    tone: TasbihCounterTone,
    stateDescription: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    targetLabel: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val tapLabel = stringResource(R.string.tasbih_counter_tap_action)
    val reached = tone == TasbihCounterTone.TARGET_REACHED
    // Revamp handoff §10: a surface-filled circle whose 1dp border is the only thing that changes at
    // the target — outline while counting, primary once reached. The count itself is the element,
    // so nothing else competes with it.
    val borderColor =
        if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onTap()
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor),
        modifier =
            modifier
                .size(SanguSantriDimensions.tasbihCounterSize)
                .semantics {
                    contentDescription = tapLabel
                    this.stateDescription = stateDescription
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(SanguSantriSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = count.toString(),
                fontSize = COUNT_SIZE_SP.sp,
                lineHeight = (COUNT_SIZE_SP + 4).sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp,
                color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (targetLabel != null) {
                Text(
                    text = targetLabel,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val COUNT_SIZE_SP = 74

@PreviewLightDark
@Composable
private fun TasbihCounterCountingPreview() {
    SanguSantriTheme {
        TasbihCounter(
            count = 12,
            tone = TasbihCounterTone.COUNTING,
            stateDescription = "12 dari 33, sedang menghitung",
            onTap = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TasbihCounterTargetReachedPreview() {
    SanguSantriTheme {
        TasbihCounter(
            count = 33,
            tone = TasbihCounterTone.TARGET_REACHED,
            stateDescription = "33 dari 33, target tercapai",
            onTap = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TasbihCounterNeutralPreview() {
    SanguSantriTheme {
        TasbihCounter(count = 0, tone = TasbihCounterTone.NEUTRAL, stateDescription = "0, belum ada target", onTap = {})
    }
}
