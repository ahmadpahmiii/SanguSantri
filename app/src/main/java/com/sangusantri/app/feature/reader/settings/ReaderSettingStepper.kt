package com.sangusantri.app.feature.reader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R

private val STEPPER_VALUE_MIN_WIDTH = 48.dp

/** A stepper's mutable parts, bundled to keep [ReaderSettingStepper]'s parameter list small. */
data class ReaderStepperControl(
    val valueText: String,
    val onDecrease: () -> Unit,
    val onIncrease: () -> Unit,
    val decreaseEnabled: Boolean,
    val increaseEnabled: Boolean,
)

/** Grouped label + decrement/increment control with a visible current value (restrained, no slider). */
@Composable
fun ReaderSettingStepper(
    label: String,
    control: ReaderStepperControl,
    modifier: Modifier = Modifier,
) {
    val decreaseDescription = stringResource(R.string.reader_settings_decrease_content_description, label)
    val increaseDescription = stringResource(R.string.reader_settings_increase_content_description, label)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = control.onDecrease,
                enabled = control.decreaseEnabled,
                modifier = Modifier.semantics { contentDescription = decreaseDescription },
            ) {
                Text(text = "−", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = control.valueText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = STEPPER_VALUE_MIN_WIDTH),
            )
            IconButton(
                onClick = control.onIncrease,
                enabled = control.increaseEnabled,
                modifier = Modifier.semantics { contentDescription = increaseDescription },
            ) {
                Text(text = "+", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
