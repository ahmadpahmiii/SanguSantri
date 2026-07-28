package com.sangusantri.app.feature.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import kotlinx.coroutines.delay

private const val SAVED_POSITION_VISIBLE_MILLIS = 2_500L

/** One-shot flag: shows the saved-position pill only once, if the reader actually resumed mid-way. */
@Composable
fun rememberInitialSavedPositionFlag(initialItemIndex: Int): Boolean {
    var initialPositionChecked by rememberSaveable { mutableStateOf(false) }
    var showSavedPosition by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!initialPositionChecked) {
            initialPositionChecked = true
            if (initialItemIndex > 0) showSavedPosition = true
        }
    }
    return showSavedPosition
}

/**
 * Brief confirmation pill shown once when a reader resumes at a previously saved position
 * (`docs/design/FIGMA_HANDOFF.md` nodes `14:2`/`14:32`, "✓ Posisi bacaan tersimpan") — transient,
 * not a permanent fixture, since the underlying save event already happened before this composes.
 */
@Composable
fun ReaderSavedPositionStatus(
    show: Boolean,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            visible = true
            delay(SAVED_POSITION_VISIBLE_MILLIS)
            visible = false
        }
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Surface(
            shape = SanguSantriShapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
                modifier =
                    Modifier.padding(
                        horizontal = SanguSantriSpacing.default,
                        vertical = SanguSantriSpacing.medium,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.reader_saved_position_status),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
