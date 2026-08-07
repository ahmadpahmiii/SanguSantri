package com.sangusantri.app.feature.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.feature.reminder.components.NotificationPermissionBanner

@Suppress("LongParameterList")
@Composable
internal fun ReminderList(
    uiState: ReminderUiState.Loaded,
    notificationPermissionGranted: Boolean,
    permanentlyDenied: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onToggle: (Reminder) -> Unit,
    onEdit: (Reminder) -> Unit,
    onDeleteRequest: (Reminder) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.reminders.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.reminder_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val hijriMonthNames = stringArrayResource(R.array.reminder_hijri_month_names).toList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        if (!notificationPermissionGranted) {
            item {
                NotificationPermissionBanner(
                    permanentlyDenied = permanentlyDenied,
                    onRequestPermission = onRequestNotificationPermission,
                    onOpenSettings = onOpenNotificationSettings,
                    modifier = Modifier.padding(bottom = SanguSantriSpacing.small),
                )
            }
        }
        items(items = uiState.reminders, key = { it.id }) { reminder ->
            ReminderRow(
                reminder = reminder,
                contentTitle = uiState.contentTitleFor(reminder.contentId),
                hijriMonthNames = hijriMonthNames,
                actions =
                    ReminderRowActions(
                        onToggle = { onToggle(reminder) },
                        onEdit = { onEdit(reminder) },
                        onDelete = { onDeleteRequest(reminder) },
                    ),
            )
        }
    }
}

private data class ReminderRowActions(
    val onToggle: () -> Unit,
    val onEdit: () -> Unit,
    val onDelete: () -> Unit,
)

@Composable
private fun ReminderRow(
    reminder: Reminder,
    contentTitle: String?,
    hijriMonthNames: List<String>,
    actions: ReminderRowActions,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = actions.onEdit,
        shape = SanguSantriShapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.label.ifBlank { contentTitle ?: reminder.contentId },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (contentTitle != null && reminder.label.isNotBlank()) {
                        Text(
                            text = contentTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(checked = reminder.isEnabled, onCheckedChange = { actions.onToggle() })
            }
            Text(
                text = ReminderScheduleFormatter.formatScheduleSummary(reminder.schedule, hijriMonthNames),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = ReminderScheduleFormatter.formatNextTriggerHijri(reminder, hijriMonthNames),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = actions.onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.reminder_delete_action),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
