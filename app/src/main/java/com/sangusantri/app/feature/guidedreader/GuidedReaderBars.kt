package com.sangusantri.app.feature.guidedreader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GuidedReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.reader_settings_content_description),
                )
            }
        },
    )
}

@Composable
internal fun GuidedReaderBottomBar(
    state: GuidedReaderUiState.StepVisible,
    onPrevious: () -> Unit,
    onContinue: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SanguSantriSpacing.default),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !state.isFirstStep,
            modifier = Modifier.weight(1f),
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text(
                text = stringResource(R.string.guided_reader_previous_action),
                modifier = Modifier.padding(start = SanguSantriSpacing.extraSmall),
            )
        }
        val continueLabel =
            if (state.isLastStep) {
                stringResource(R.string.guided_reader_finish_action)
            } else {
                stringResource(R.string.guided_reader_continue_action)
            }
        val continueEnabled = if (state.isLastStep) state.allRequiredCountersComplete else state.continueEnabled
        Button(
            onClick = onContinue,
            enabled = continueEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = continueLabel)
        }
    }
}
