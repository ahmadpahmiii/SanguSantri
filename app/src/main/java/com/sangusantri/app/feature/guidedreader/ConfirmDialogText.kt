package com.sangusantri.app.feature.guidedreader

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Bundles [GuidedConfirmDialog]'s four text values so the function stays under the parameter-count limit. */
internal data class ConfirmDialogText(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val cancelLabel: String,
)

/** Shared confirmation dialog shape for the counter-reset and completion confirmations (FR-006/FR-007). */
@Composable
internal fun GuidedConfirmDialog(
    text: ConfirmDialogText,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = text.title) },
        text = { Text(text = text.message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = text.confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = text.cancelLabel) } },
    )
}
