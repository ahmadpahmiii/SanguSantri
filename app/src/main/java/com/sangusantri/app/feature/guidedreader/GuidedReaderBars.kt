package com.sangusantri.app.feature.guidedreader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * Reader appearance settings moved into the shared overflow menu (decision F, Figma
 * product-alignment pass) — this top bar no longer carries its own standalone settings icon,
 * only back navigation and the [overflow] slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GuidedReaderTopBar(
    title: String,
    onBack: () -> Unit,
    overflow: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title) },
        expandedHeight = SanguSantriDimensions.compactTopAppBarHeight,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = { overflow() },
    )
}

/**
 * Both actions render as filled, stadium-shaped pills (`docs/design/FIGMA_HANDOFF.md` node
 * `14:32`) — "Sebelumnya" tonal (`primaryContainer`), "Lanjut"/"Selesaikan" primary — rather than
 * an outlined/filled pairing. Direction is conveyed by an [Icons.AutoMirrored] arrow (RTL-correct)
 * rather than a literal arrow glyph in the label, per `docs/design/ACCESSIBILITY.md`'s RTL rule.
 */
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
        Button(
            onClick = onPrevious,
            enabled = !state.isFirstStep,
            shape = SanguSantriShapes.extraLarge,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
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
            shape = SanguSantriShapes.extraLarge,
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
        ) {
            Text(text = continueLabel)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(start = SanguSantriSpacing.extraSmall),
            )
        }
    }
}
