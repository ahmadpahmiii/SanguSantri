package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.domain.model.TasbihTargetPreset

/**
 * The compact target chip row (`Target Selector`/`Preset Group`, `docs/design/DESIGN_SYSTEM.md`'s
 * Tasbih target hierarchy — a segmented chip row, deliberately never large preset cards). No 99
 * preset. The Custom chip never shows as selected itself, even while a custom target is the active
 * one — the numeral moves to the Target Header instead; this chip is only ever the dialog trigger.
 */
@Composable
fun TasbihTargetSelector(
    selectedPreset: TasbihTargetPreset?,
    onPresetSelected: (TasbihTargetPreset) -> Unit,
    onCustomRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Revamp handoff §10: one segmented container (surface + 1dp outline, 17dp radius) rather than
    // four loose chips, so the four choices read as one control.
    Surface(
        shape = RoundedCornerShape(ContainerCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(SegmentGap)) {
            TasbihPresetSegment(
                label = stringResource(R.string.tasbih_preset_thirty_three),
                contentDescription = null,
                selected = selectedPreset == TasbihTargetPreset.THIRTY_THREE,
                onClick = { onPresetSelected(TasbihTargetPreset.THIRTY_THREE) },
                modifier = Modifier.weight(1f),
            )
            TasbihPresetSegment(
                label = stringResource(R.string.tasbih_preset_one_hundred),
                contentDescription = null,
                selected = selectedPreset == TasbihTargetPreset.ONE_HUNDRED,
                onClick = { onPresetSelected(TasbihTargetPreset.ONE_HUNDRED) },
                modifier = Modifier.weight(1f),
            )
            TasbihPresetSegment(
                label = stringResource(R.string.tasbih_preset_unlimited_symbol),
                contentDescription = stringResource(R.string.tasbih_preset_unlimited_description),
                selected = selectedPreset == TasbihTargetPreset.UNLIMITED,
                onClick = { onPresetSelected(TasbihTargetPreset.UNLIMITED) },
                modifier = Modifier.weight(1f),
            )
            // Never shows as selected itself even while a custom target is active — the numeral
            // moves to the counter's own caption instead; this segment is only the dialog trigger.
            TasbihPresetSegment(
                label = stringResource(R.string.tasbih_preset_custom),
                contentDescription = null,
                selected = false,
                onClick = onCustomRequested,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TasbihPresetSegment(
    label: String,
    contentDescription: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(SegmentCornerRadius),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor =
            if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier
                .height(SegmentHeight)
                .semantics {
                    this.selected = selected
                    contentDescription?.let { this.contentDescription = it }
                },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val ContainerCornerRadius = 17.dp
private val SegmentCornerRadius = 14.dp
private val SegmentHeight = 34.dp
private val SegmentGap = 3.dp
