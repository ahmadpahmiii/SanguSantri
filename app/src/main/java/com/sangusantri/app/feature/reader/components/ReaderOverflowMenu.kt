package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.reader.ApprovalDisplay

/**
 * Shared reader top-bar overflow menu (Milestone 5 FR-016, Milestone 6 source/approval split): the
 * mode-switch action, plus a compact "Sumber & Pentashihan" info dialog. Source attribution is
 * always shown, truthfully, for every amaliyah (PRD 6.5) — the compact `Approved by` line appears
 * only when real religious-authority approval metadata exists; neither is ever fabricated.
 * Deliberately not visually dominant — an overflow action, never a bottom navigation bar or a card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderOverflowMenu(
    switchModeLabel: String,
    onSwitchMode: () -> Unit,
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

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(switchModeLabel) },
            onClick = {
                expanded = false
                onSwitchMode()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.content_approval_menu_action)) },
            onClick = {
                expanded = false
                showSourceInfo = true
            },
        )
    }

    if (showSourceInfo) {
        SourceAndApprovalInfoDialog(
            sourceName = sourceName,
            approvalDisplay = approvalDisplay,
            onDismiss = { showSourceInfo = false },
        )
    }
}

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
