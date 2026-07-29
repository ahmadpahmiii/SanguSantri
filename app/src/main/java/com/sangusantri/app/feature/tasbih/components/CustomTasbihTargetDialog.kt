package com.sangusantri.app.feature.tasbih.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.TasbihTargetPreset

/**
 * A small numeric-input dialog (never a full-screen form, `DESIGN_SYSTEM.md` decision J) that
 * rejects an invalid target before dismissal is possible — [onConfirm] can only ever be invoked
 * with an already-valid value (`ACCESSIBILITY.md`'s numeric-input rule).
 */
@Composable
fun CustomTasbihTargetDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val validation = validateCustomTarget(input)

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = SanguSantriShapes.large,
        title = { Text(text = stringResource(R.string.tasbih_custom_target_dialog_title)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.tasbih_custom_target_input_label)) },
                suffix = { Text(text = stringResource(R.string.tasbih_custom_target_unit)) },
                singleLine = true,
                isError = validation != CustomTargetValidation.VALID,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    customTargetErrorMessage(validation)?.let { message ->
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { input.trim().toIntOrNull()?.let(onConfirm) },
                enabled = validation == CustomTargetValidation.VALID,
            ) {
                Text(text = stringResource(R.string.tasbih_custom_target_save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.tasbih_custom_target_cancel_action))
            }
        },
    )
}

@Composable
private fun customTargetErrorMessage(validation: CustomTargetValidation): String? =
    when (validation) {
        CustomTargetValidation.VALID -> null
        CustomTargetValidation.EMPTY, CustomTargetValidation.ZERO, CustomTargetValidation.NEGATIVE ->
            stringResource(R.string.tasbih_custom_target_error_must_be_positive)

        CustomTargetValidation.NON_NUMERIC -> stringResource(R.string.tasbih_custom_target_error_non_numeric)
        CustomTargetValidation.TOO_LARGE ->
            stringResource(R.string.tasbih_custom_target_error_too_large, TasbihTargetPreset.MAX_CUSTOM_TARGET)
    }

@Preview
@Composable
private fun CustomTasbihTargetDialogPreview() {
    SanguSantriTheme {
        CustomTasbihTargetDialog(onDismiss = {}, onConfirm = {})
    }
}
