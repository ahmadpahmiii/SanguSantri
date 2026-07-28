package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.reader.ApprovalDisplay

/**
 * Shared reader top-bar overflow menu (Milestone 5 FR-016, Milestone 6 source/approval split,
 * Figma product-alignment pass nodes `16:2`/`16:45`): mode-switch, Table of Contents (FR-017),
 * reader appearance settings (moved here from a standalone top-bar icon — decision F), and a
 * compact "Sumber & Pentashihan" info dialog, in that order. Source attribution is always shown,
 * truthfully, for every amaliyah (PRD 6.5) — the compact `Approved by` line appears only when real
 * religious-authority approval metadata exists; neither is ever fabricated. Deliberately not
 * visually dominant — an overflow action, never a bottom navigation bar or a card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderOverflowMenu(
    switchModeLabel: String,
    actions: ReaderOverflowActions,
    sourceName: String,
    approvalDisplay: ApprovalDisplay,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var showSourceInfo by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.reader_overflow_content_description),
        )
    }

    ReaderOverflowDropdown(
        switchModeLabel = switchModeLabel,
        actions = actions,
        expanded = expanded,
        onDismiss = { expanded = false },
        onOpenSource = {
            expanded = false
            showSourceInfo = true
        },
    )

    if (showSourceInfo) {
        SourceAndApprovalInfoDialog(
            sourceName = sourceName,
            approvalDisplay = approvalDisplay,
            onDismiss = { showSourceInfo = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderOverflowDropdown(
    switchModeLabel: String,
    actions: ReaderOverflowActions,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenSource: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(SanguSantriDimensions.overflowMenuWidth),
        shape = SanguSantriShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        ReaderMenuItem(switchModeLabel, Icons.AutoMirrored.Filled.ArrowForward) {
            onDismiss()
            actions.onSwitchMode()
        }
        ReaderMenuItem(stringResource(R.string.reader_open_toc_action), Icons.AutoMirrored.Filled.List) {
            onDismiss()
            actions.onOpenTableOfContents()
        }
        ReaderMenuItem(stringResource(R.string.reader_open_settings_action), Icons.Default.Settings) {
            onDismiss()
            actions.onOpenSettings()
        }
        ReaderMenuItem(stringResource(R.string.content_approval_menu_action), Icons.Default.Info, onOpenSource)
    }
}

@Composable
private fun ReaderMenuItem(
    label: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = { ReaderMenuIcon(imageVector) },
        onClick = onClick,
        modifier = Modifier.height(MENU_ITEM_HEIGHT),
    )
}

@Composable
private fun ReaderMenuIcon(imageVector: ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(MENU_ICON_SIZE),
    )
}

private val MENU_ITEM_HEIGHT = 52.dp
private val MENU_ICON_SIZE = 18.dp

@Composable
private fun SourceAndApprovalInfoDialog(
    sourceName: String,
    approvalDisplay: ApprovalDisplay,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.content_approval_menu_action)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.content_source_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                )
                when (approvalDisplay) {
                    is ApprovalDisplay.Approved -> {
                        Text(
                            text = stringResource(R.string.content_approved_by_label),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = SanguSantriSpacing.small),
                        )
                        Text(
                            text = approvalDisplay.approverLabel,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                        )
                    }

                    ApprovalDisplay.Pending ->
                        Text(
                            text = stringResource(R.string.content_approval_pending_dev_only),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = SanguSantriSpacing.small),
                        )

                    ApprovalDisplay.Hidden -> Unit
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.reader_settings_close_action))
            }
        },
    )
}
