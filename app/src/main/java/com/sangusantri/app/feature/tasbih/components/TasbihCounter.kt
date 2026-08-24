package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
 * cycle — a deliberate difference, not an inconsistency, so [onTap] is never guarded here. Nothing
 * is lost by it: the finished round was archived to Riwayat the moment it reached its target
 * (`TasbihRepositoryImpl.incrementCount`).
 */
@Composable
fun TasbihCounter(
    count: Int,
    tone: TasbihCounterTone,
    stateDescription: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    targetLabel: String? = null,
    /** `0f`..`1f` of the way to the target, or `null` when there is no target to be on the way to. */
    progress: Float? = null,
) {
    val haptics = LocalHapticFeedback.current
    val tapLabel = stringResource(R.string.tasbih_counter_tap_action)
    val reached = tone == TasbihCounterTone.TARGET_REACHED
    // Revamp handoff §10: a surface-filled circle whose 1dp border is the only thing that changes at
    // the target — outline while counting, primary once reached. The count itself is the element,
    // so nothing else competes with it.
    val borderColor =
        if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(modifier = modifier.size(SanguSantriDimensions.tasbihCounterSize), contentAlignment = Alignment.Center) {
        Surface(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onTap()
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, borderColor),
            modifier =
                Modifier
                    .fillMaxSize()
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
        if (progress != null) TasbihProgressRing(progress)
    }
}

/**
 * Drawn over the circle's own hairline border, so how far the round has to go is part of the
 * counter itself rather than a number the reader has to do arithmetic on. The Surface underneath
 * keeps the whole area tappable; this arc is decoration only.
 */
@Composable
private fun TasbihProgressRing(progress: Float) {
    CircularProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = Color.Transparent,
        strokeWidth = ProgressStrokeWidth,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
    )
}

private val ProgressStrokeWidth = 3.dp

/**
 * [TasbihCounter] plus the caption under it, wired to a live session: the target-reached caption
 * ("ketuk untuk mengulang") replaces the tap hint, and the ring shows how far the round has to go.
 */
@Composable
fun TasbihActiveCounter(
    count: Int,
    targetValue: Int?,
    isTargetReached: Boolean,
    onTap: () -> Unit,
) {
    val targetShortText = targetValue?.toString() ?: stringResource(R.string.tasbih_target_unlimited_short)
    val statusText =
        stringResource(
            if (isTargetReached) R.string.tasbih_status_target_reached else R.string.tasbih_status_counting,
        )
    TasbihCounter(
        count = count,
        tone = if (isTargetReached) TasbihCounterTone.TARGET_REACHED else TasbihCounterTone.COUNTING,
        stateDescription =
            stringResource(R.string.tasbih_counter_state_description, count, targetShortText, statusText),
        onTap = onTap,
        targetLabel = targetValue?.let { stringResource(R.string.tasbih_counter_of_target, it) },
        progress = targetValue?.let { (count.toFloat() / it).coerceIn(0f, 1f) },
    )
    Text(
        text =
            stringResource(
                if (isTargetReached) {
                    R.string.tasbih_counter_caption_target_reached
                } else {
                    R.string.tasbih_counter_tap_hint
                },
            ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
            progress = 12f / 33f,
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
            progress = 1f,
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
