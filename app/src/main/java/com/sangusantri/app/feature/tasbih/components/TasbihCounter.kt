package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
) {
    val haptics = LocalHapticFeedback.current
    val tapLabel = stringResource(R.string.tasbih_counter_tap_action)
    val captionRes =
        if (tone == TasbihCounterTone.TARGET_REACHED) {
            R.string.tasbih_counter_caption_target_reached
        } else {
            R.string.tasbih_counter_caption_counting
        }
    val (containerColor, contentColor) = tasbihCounterColors(tone)

    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onTap()
        },
        shape = SanguSantriShapes.extraLarge,
        color = containerColor,
        modifier =
            modifier
                .sizeIn(
                    minWidth = SanguSantriDimensions.tasbihCounterMinSize,
                    minHeight = SanguSantriDimensions.tasbihCounterMinSize,
                )
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
            if (tone == TasbihCounterTone.TARGET_REACHED) {
                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = contentColor)
            }
            Text(
                text = count.toString(),
                fontSize = 72.sp,
                lineHeight = 78.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                text = stringResource(captionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = CAPTION_ALPHA),
            )
        }
    }
}

@Composable
private fun tasbihCounterColors(tone: TasbihCounterTone): Pair<Color, Color> =
    when (tone) {
        TasbihCounterTone.NEUTRAL ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant

        TasbihCounterTone.COUNTING ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        TasbihCounterTone.TARGET_REACHED ->
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

private const val CAPTION_ALPHA = 0.8f

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
