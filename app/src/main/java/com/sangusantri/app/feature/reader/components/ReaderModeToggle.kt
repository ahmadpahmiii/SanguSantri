package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriPillShape
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.ReaderMode

/**
 * Shared Bacaan Lengkap ⇄ Panduan switch (FR-016): one pinned two-segment control rendered
 * directly under both readers' top bars. Switching is a single tap in either direction and the
 * active mode is always visible — replacing the Full Reader's one-way tint pill plus the overflow
 * menu's mode item, which made the reverse switch (Panduan → Lengkap) a two-step menu action.
 *
 * Both directions are position-preserving and instantly reversible (`ReaderViewModel.switchToGuided`
 * / `GuidedReaderViewModel.onSwitchToFull` hand the current step to the other mode), so the switch
 * is deliberately immediate: no confirmation dialog, no destination-only label.
 */
@Composable
fun ReaderModeToggle(
    current: ReaderMode,
    onSelect: (ReaderMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SanguSantriDimensions.readerHorizontalPadding,
                    vertical = SanguSantriSpacing.small,
                ),
        contentAlignment = Alignment.Center,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxWidth(),
        ) {
            ReaderMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == current,
                    onClick = { if (mode != current) onSelect(mode) },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ReaderMode.entries.size,
                            baseShape = SanguSantriPillShape,
                        ),
                    colors =
                        SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    // No selected-state check icon: the container tint already carries the state,
                    // and the labels stay readable at small widths without it.
                    icon = {},
                    modifier = Modifier.height(SanguSantriDimensions.minimumTouchTarget),
                    label = {
                        Text(
                            text = stringResource(mode.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                        )
                    },
                )
            }
        }
    }
}

private val ReaderMode.labelRes: Int
    get() =
        when (this) {
            ReaderMode.FULL -> R.string.reader_mode_toggle_full
            ReaderMode.GUIDED -> R.string.reader_mode_toggle_guided
        }
