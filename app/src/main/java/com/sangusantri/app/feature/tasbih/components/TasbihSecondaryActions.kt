package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/** The Tasbih secondary actions row (Reset/Riwayat chips) — Reset is hidden while there is nothing
 * to reset (state 1, "Belum Ada Sesi"); Riwayat is always present. */
@Composable
fun TasbihSecondaryActions(
    showReset: Boolean,
    onResetClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        if (showReset) {
            AssistChip(onClick = onResetClick, label = { Text(text = stringResource(R.string.tasbih_reset_action)) })
        }
        AssistChip(onClick = onHistoryClick, label = { Text(text = stringResource(R.string.tasbih_history_action)) })
    }
}
