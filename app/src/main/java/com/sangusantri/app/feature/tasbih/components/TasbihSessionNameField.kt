package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions

/**
 * The optional Tasbih session name row — a plain row reading the current name (or "Tanpa nama")
 * that expands into an inline text field on tap (design spec: "tap → inline text field", never a
 * separate dialog).
 */
@Composable
fun TasbihSessionNameField(
    sessionName: String?,
    onRename: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    if (isEditing) {
        var draft by rememberSaveable(sessionName) { mutableStateOf(sessionName.orEmpty()) }
        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier =
                modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            label = { Text(text = stringResource(R.string.tasbih_session_name_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        onRename(draft.trim().ifBlank { null })
                        isEditing = false
                    },
                ),
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    } else {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                    .clickable(onClick = { isEditing = true }),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sessionName ?: stringResource(R.string.tasbih_session_name_empty),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.tasbih_session_name_action),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
