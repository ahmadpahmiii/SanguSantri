package com.sangusantri.app.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * The shared "Confirmation Dialog Shell" (`01-navigation-and-shared-components.md`) for short,
 * focused decisions — Tasbih's reset confirmation now, Pengingat's delete confirmation later
 * (`docs/design/DESIGN_SYSTEM.md`'s "Dialog: reserved for short, focused decisions" rule). Flat,
 * no shadow elevation, per the project's existing elevation policy. [isDestructive] tints the
 * confirm action (`SantriError40`/`onError`) for irreversible actions such as delete/reset.
 */
@Composable
fun ConfirmationDialog(
    text: ConfirmationDialogText,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = SanguSantriShapes.large,
        tonalElevation = SanguSantriElevation.flat,
        title = { Text(text = text.title) },
        text = { Text(text = text.message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors =
                    if (isDestructive) {
                        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.textButtonColors()
                    },
            ) {
                Text(text = text.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = text.cancelLabel)
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogPreview() {
    SanguSantriTheme {
        ConfirmationDialog(
            text =
                ConfirmationDialogText(
                    title = "Reset hitungan?",
                    message = "Hitungan saat ini (12) akan dihapus dan tidak dapat dikembalikan.",
                    confirmLabel = "Reset",
                    cancelLabel = "Batal",
                ),
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}
