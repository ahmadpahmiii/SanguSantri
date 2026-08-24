package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * Shown the moment a target is reached. It exists because reaching 33 of 33 used to change nothing
 * except a border colour: the round was already over, nothing said so, and nothing said it had been
 * saved. The panel names both, and offers the only two things there are to do next.
 */
@Composable
fun TasbihCompletionPanel(
    finalCount: Int,
    onRepeat: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(PanelCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
        ) {
            Text(
                text = stringResource(R.string.tasbih_completed_title, finalCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.tasbih_completed_saved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            ) {
                Button(onClick = onRepeat) {
                    Text(text = stringResource(R.string.tasbih_completed_repeat_action))
                }
                TextButton(onClick = onHistoryClick) {
                    Text(text = stringResource(R.string.tasbih_completed_history_action))
                }
            }
        }
    }
}

private val PanelCornerRadius = 20.dp

@PreviewLightDark
@Composable
private fun TasbihCompletionPanelPreview() {
    SanguSantriTheme {
        TasbihCompletionPanel(
            finalCount = 33,
            onRepeat = {},
            onHistoryClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
