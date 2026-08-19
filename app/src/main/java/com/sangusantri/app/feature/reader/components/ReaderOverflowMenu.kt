package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

/**
 * Shared reader top-bar overflow menu (Milestone 5 FR-016, design product-alignment pass nodes
 * `16:2`/`16:45`): mode-switch, reader appearance settings (moved here from a standalone top-bar
 * icon — decision F), and a compact source-attribution dialog, in that order. Source attribution
 * is always shown, truthfully, for every content item (PRD 6.5); ADR 0015 dropped the separate
 * religious-authority approval object from the Android model, so this menu no longer has an
 * "Approved by" line to show — only the source name remains. Deliberately not visually dominant —
 * an overflow action, never a bottom navigation bar or a card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderOverflowMenu(
    switchModeLabel: String?,
    actions: ReaderOverflowActions,
    sourceName: String,
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
        SourceInfoDialog(sourceName = sourceName, onDismiss = { showSourceInfo = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderOverflowDropdown(
    switchModeLabel: String?,
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
        // null when the content has no Panduan mode to switch to (no step has a repeat target),
        // in which case the item is absent rather than disabled — there is nothing to explain.
        if (switchModeLabel != null) {
            ReaderMenuItem(switchModeLabel, Icons.AutoMirrored.Filled.ArrowForward) {
                onDismiss()
                actions.onSwitchMode()
            }
        }
        ReaderMenuItem(stringResource(R.string.reader_open_settings_action), Icons.Default.Settings) {
            onDismiss()
            actions.onOpenSettings()
        }
        ReaderMenuItem(stringResource(R.string.content_source_menu_action), Icons.Default.Info, onOpenSource)
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
private fun SourceInfoDialog(
    sourceName: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.content_source_menu_action)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.content_source_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.reader_settings_close_action))
            }
        },
    )
}
