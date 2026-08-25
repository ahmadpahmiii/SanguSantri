@file:Suppress("MatchingDeclarationName")

package com.sangusantri.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

data class PermissionSheetActions(
    val onGrantPermissions: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onDismiss: () -> Unit,
)

/**
 * Bottom sheet dialog presented on subsequent app launches when required permissions
 * (Location for Kiblat/Prayer Times or Notification for Adzan) are missing.
 *
 * Explains clearly why each permission is needed and offers either direct request or opening
 * system app settings if permanently denied.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBottomSheet(
    showLocation: Boolean,
    showNotification: Boolean,
    permanentlyDenied: Boolean,
    actions: PermissionSheetActions,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
        sheetState = sheetState,
        shape = SanguSantriShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.large)
                    .padding(bottom = SanguSantriSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PermissionSheetHeader()
            Spacer(modifier = Modifier.height(SanguSantriSpacing.large))
            PermissionSheetItems(showLocation = showLocation, showNotification = showNotification)
            Spacer(modifier = Modifier.height(SanguSantriSpacing.large))
            PermissionSheetButtons(permanentlyDenied = permanentlyDenied, actions = actions)
        }
    }
}

@Composable
private fun PermissionSheetHeader() {
    Box(
        modifier =
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(28.dp),
        )
    }

    Spacer(modifier = Modifier.height(SanguSantriSpacing.medium))

    Text(
        text = stringResource(R.string.permission_bottom_sheet_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))

    Text(
        text = stringResource(R.string.permission_bottom_sheet_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PermissionSheetItems(
    showLocation: Boolean,
    showNotification: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (showLocation) {
            PermissionItemRow(
                icon = Icons.Outlined.Explore,
                title = stringResource(R.string.permission_location_title),
                description = stringResource(R.string.permission_location_description),
            )
        }

        if (showNotification) {
            PermissionItemRow(
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.permission_notification_title),
                description = stringResource(R.string.permission_notification_description),
            )
        }
    }
}

@Composable
private fun PermissionSheetButtons(
    permanentlyDenied: Boolean,
    actions: PermissionSheetActions,
) {
    Button(
        onClick = if (permanentlyDenied) actions.onOpenSettings else actions.onGrantPermissions,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(48.dp),
        shape = SanguSantriShapes.medium,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(
            text =
                stringResource(
                    if (permanentlyDenied) {
                        R.string.permission_action_settings
                    } else {
                        R.string.permission_action_grant
                    },
                ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }

    Spacer(modifier = Modifier.height(SanguSantriSpacing.small))

    TextButton(
        onClick = actions.onDismiss,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.permission_action_dismiss),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionItemRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SanguSantriShapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PermissionBottomSheetPreview() {
    SanguSantriTheme {
        PermissionBottomSheet(
            showLocation = true,
            showNotification = true,
            permanentlyDenied = false,
            actions =
                PermissionSheetActions(
                    onGrantPermissions = {},
                    onOpenSettings = {},
                    onDismiss = {},
                ),
        )
    }
}
