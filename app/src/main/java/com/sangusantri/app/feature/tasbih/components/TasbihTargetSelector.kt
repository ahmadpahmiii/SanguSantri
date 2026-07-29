package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
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
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        TasbihPresetChip(
            label = stringResource(R.string.tasbih_preset_thirty_three),
            contentDescription = null,
            selected = selectedPreset == TasbihTargetPreset.THIRTY_THREE,
            onClick = { onPresetSelected(TasbihTargetPreset.THIRTY_THREE) },
        )
        TasbihPresetChip(
            label = stringResource(R.string.tasbih_preset_one_hundred),
            contentDescription = null,
            selected = selectedPreset == TasbihTargetPreset.ONE_HUNDRED,
            onClick = { onPresetSelected(TasbihTargetPreset.ONE_HUNDRED) },
        )
        TasbihPresetChip(
            label = stringResource(R.string.tasbih_preset_unlimited_symbol),
            contentDescription = stringResource(R.string.tasbih_preset_unlimited_description),
            selected = selectedPreset == TasbihTargetPreset.UNLIMITED,
            onClick = { onPresetSelected(TasbihTargetPreset.UNLIMITED) },
        )
        TasbihPresetChip(
            label = stringResource(R.string.tasbih_preset_custom),
            contentDescription = null,
            selected = false,
            onClick = onCustomRequested,
        )
    }
}

@Composable
private fun TasbihPresetChip(
    label: String,
    contentDescription: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .semantics { contentDescription?.let { this.contentDescription = it } },
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        shape = SanguSantriShapes.extraLarge,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurface,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.5.dp,
            ),
    )
}

@PreviewLightDark
@Composable
private fun TasbihTargetSelectorPreview() {
    SanguSantriTheme {
        TasbihTargetSelector(
            selectedPreset = TasbihTargetPreset.THIRTY_THREE,
            onPresetSelected = {},
            onCustomRequested = {},
        )
    }
}
