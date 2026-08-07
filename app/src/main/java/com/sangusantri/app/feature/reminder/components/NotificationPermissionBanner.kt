package com.sangusantri.app.feature.reminder.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * Persistent, dismissible-by-fixing-the-problem inline nudge — never a blocking dialog, and
 * reminder creation/editing is never gated on it (ROADMAP.md's "notification permission flow";
 * `NotificationManagerCompat` already no-ops safely without the permission, so this is purely an
 * honest status + a way to fix it). [permanentlyDenied] switches the action from the system
 * permission request to opening the app's notification settings, since a second in-app request
 * would not show a system dialog at all once permanently denied.
 */
@Composable
fun NotificationPermissionBanner(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SanguSantriShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        ) {
            Text(
                text = stringResource(R.string.reminder_notification_banner_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.reminder_notification_banner_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = if (permanentlyDenied) onOpenSettings else onRequestPermission) {
                Text(
                    text =
                        stringResource(
                            if (permanentlyDenied) {
                                R.string.reminder_notification_banner_open_settings_action
                            } else {
                                R.string.reminder_notification_banner_enable_action
                            },
                        ),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationPermissionBannerPreview() {
    SanguSantriTheme {
        NotificationPermissionBanner(permanentlyDenied = false, onRequestPermission = {}, onOpenSettings = {})
    }
}
