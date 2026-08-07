package com.sangusantri.app.feature.reminder

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ConfirmationDialog
import com.sangusantri.app.core.designsystem.component.ConfirmationDialogText
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.feature.reminder.components.ReminderFormSheet

/** [ReminderScreen]'s two modal overlays — split out to keep that composable short. */
@Composable
internal fun ReminderFormOverlay(
    uiState: ReminderUiState.Loaded,
    formTarget: FormTarget,
    onAction: (ReminderUiAction) -> Unit,
    onDismiss: () -> Unit,
) {
    ReminderFormSheet(
        availableContent = uiState.availableContent,
        existing = (formTarget as? FormTarget.Edit)?.reminder,
        onSave = { reminder ->
            onAction(ReminderUiAction.SaveReminder(reminder))
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@Composable
internal fun ReminderDeleteOverlay(
    reminder: Reminder,
    onAction: (ReminderUiAction) -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        text =
            ConfirmationDialogText(
                title = stringResource(R.string.reminder_delete_dialog_title),
                message =
                    stringResource(
                        R.string.reminder_delete_dialog_message,
                        reminder.label.ifBlank { reminder.contentId },
                    ),
                confirmLabel = stringResource(R.string.reminder_delete_dialog_confirm),
                cancelLabel = stringResource(R.string.reminder_delete_dialog_cancel),
            ),
        onConfirm = {
            onAction(ReminderUiAction.DeleteReminder(reminder.id))
            onDismiss()
        },
        onDismiss = onDismiss,
        isDestructive = true,
    )
}
