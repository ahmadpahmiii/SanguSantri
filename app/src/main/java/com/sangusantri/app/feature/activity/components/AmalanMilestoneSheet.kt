package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * Shown once when a streak first reaches 3, 7, 30, 40, 100 or 365 days
 * ([AmalanHarian.MILESTONES][com.sangusantri.app.domain.model.AmalanHarian.MILESTONES]).
 *
 * It carries **no du'a or hadith text**. The design calls for one, but scripture needs a named
 * published source and editorial approval (`CLAUDE.md` content safety) and none has been supplied
 * — writing or paraphrasing one here is never acceptable, so the sheet says the plain, true thing
 * instead and the sourced text drops in later without touching this component's shape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmalanMilestoneSheet(
    streakDays: Int,
    recordDays: Int,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SanguSantriSpacing.large,
                        end = SanguSantriSpacing.large,
                        bottom = SanguSantriSpacing.extraLarge,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.amalan_milestone_title, streakDays),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.amalan_milestone_body, recordDays),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.amalan_milestone_dismiss))
            }
        }
    }
}
