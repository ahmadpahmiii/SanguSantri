package com.sangusantri.app.feature.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * The force-update block (ADR 0017) — not [com.sangusantri.app.core.designsystem.component.ConfirmationDialog]:
 * there is no cancel action here, by product decision the user cannot dismiss this dialog. Back
 * press and outside-tap are both disabled, and the single action re-invokes the same update flow.
 */
@Composable
fun AppUpdateForceDialog(
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        shape = SanguSantriShapes.large,
        tonalElevation = SanguSantriElevation.flat,
        title = { Text(text = stringResource(R.string.app_update_force_title)) },
        text = { Text(text = stringResource(R.string.app_update_force_message)) },
        confirmButton = {
            TextButton(onClick = onUpdateClick) {
                Text(text = stringResource(R.string.app_update_force_action))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun AppUpdateForceDialogPreview() {
    SanguSantriTheme {
        AppUpdateForceDialog(onUpdateClick = {})
    }
}
