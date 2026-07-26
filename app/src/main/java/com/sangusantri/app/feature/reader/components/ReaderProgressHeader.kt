package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import kotlin.math.roundToInt

private val ProgressTrackHeight = 5.dp

/**
 * Shared reading-progress header (`docs/design/FIGMA_HANDOFF.md` nodes `14:2`/`14:32`): a
 * "Langkah N dari total" label, a percentage, and a track/value bar — identical between Full
 * Reader and Guided Reader (one canonical reading position concept, not duplicated per mode).
 */
@Composable
fun ReaderProgressHeader(
    currentPosition: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    if (totalSteps <= 0) return
    val percent = ((currentPosition.toFloat() / totalSteps) * PERCENT_SCALE).roundToInt().coerceIn(0, PERCENT_SCALE)
    val label = stringResource(R.string.reader_progress_label, currentPosition, totalSteps)
    val percentLabel = stringResource(R.string.reader_progress_percentage, percent)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "$label, $percentLabel" },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = percentLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ProgressTrackHeight)
                    .clip(SanguSantriShapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction = percent / PERCENT_SCALE.toFloat())
                        .height(ProgressTrackHeight)
                        .clip(SanguSantriShapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private const val PERCENT_SCALE = 100
