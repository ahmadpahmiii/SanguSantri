package com.sangusantri.app.feature.reader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * Two-column pairing and bait spacing, shown only in the Sholawat reader. The bait-gap row is
 * disabled whenever pairing is not actually in effect — there are no baits to space apart in a
 * one-per-row layout, and an enabled control that changes nothing is worse than a disabled one.
 */
@Composable
internal fun ReaderSettingsSholawatLayoutRows(control: SholawatLayoutControl) {
    val pairing = control.pairingAvailable && control.twoColumn
    ReaderSettingsSwitchRow(
        label = stringResource(R.string.reader_settings_sholawat_two_column),
        caption =
            stringResource(
                if (control.pairingAvailable) {
                    R.string.reader_settings_sholawat_two_column_caption
                } else {
                    R.string.reader_settings_sholawat_two_column_unavailable
                },
            ),
        checked = control.pairingAvailable && control.twoColumn,
        enabled = control.pairingAvailable,
        onCheckedChange = control.onTwoColumnChange,
    )
    ReaderSettingsSwitchRow(
        label = stringResource(R.string.reader_settings_sholawat_bait_gap),
        caption = stringResource(R.string.reader_settings_sholawat_bait_gap_caption),
        checked = control.baitGap,
        enabled = pairing,
        onCheckedChange = control.onBaitGapChange,
    )
}

@Composable
private fun ReaderSettingsSwitchRow(
    label: String,
    caption: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
    }
}
