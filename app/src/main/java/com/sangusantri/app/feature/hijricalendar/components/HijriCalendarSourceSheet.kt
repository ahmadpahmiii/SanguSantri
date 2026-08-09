package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.HijriCalendarPalette
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.core.designsystem.theme.hijriCalendarPalette

/** The "Sumber & metode" bottom sheet (§3.2's required authority-boundary disclosure), reachable
 * from the top bar's info action on every screen state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarSourceSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val palette = hijriCalendarPalette()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape =
            RoundedCornerShape(
                topStart = SanguSantriDimensions.hijriCalendarSourceSheetCornerRadius,
                topEnd = SanguSantriDimensions.hijriCalendarSourceSheetCornerRadius,
            ),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
        ) {
            SourceSheetHeading(palette = palette)
            SourceBlock(
                title = stringResource(R.string.hijri_calendar_source_hijri_title),
                body = stringResource(R.string.hijri_calendar_source_hijri_body),
                badge = stringResource(R.string.hijri_calendar_calculation_status_umm_al_qura),
                palette = palette,
            )
            SourceBlock(
                title = stringResource(R.string.hijri_calendar_source_pasaran_agenda_title),
                body = stringResource(R.string.hijri_calendar_source_pasaran_agenda_body),
                badge = null,
                palette = palette,
            )
            Button(
                onClick = onDismiss,
                shape = SanguSantriShapes.medium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = SanguSantriSpacing.default, bottom = SanguSantriSpacing.default),
            ) {
                Text(text = stringResource(R.string.hijri_calendar_source_confirm_action))
            }
        }
    }
}

@Composable
private fun SourceSheetHeading(palette: HijriCalendarPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(SanguSantriDimensions.hijriCalendarSourceIconSize)
                    .clip(SanguSantriShapes.medium)
                    .background(palette.tealSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "i", style = MaterialTheme.typography.titleMedium, color = palette.teal)
        }
        Column(modifier = Modifier.padding(start = SanguSantriSpacing.small)) {
            Text(
                text = stringResource(R.string.hijri_calendar_source_sheet_eyebrow),
                style = MaterialTheme.typography.labelLarge,
                color = palette.teal,
            )
            Text(
                text = stringResource(R.string.hijri_calendar_source_sheet_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun SourceBlock(
    title: String,
    body: String,
    badge: String?,
    palette: HijriCalendarPalette,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SanguSantriSpacing.small)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, SanguSantriShapes.medium)
                .padding(SanguSantriSpacing.default),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(
                    top = SanguSantriSpacing.extraSmall,
                    bottom =
                        if (badge !=
                            null
                        ) {
                            SanguSantriSpacing.small
                        } else {
                            0.dp
                        },
                ),
        )
        if (badge != null) {
            Box(
                modifier =
                    Modifier
                        .clip(SanguSantriShapes.extraLarge)
                        .background(palette.tealSoft)
                        .padding(horizontal = SanguSantriSpacing.small, vertical = 3.dp),
            ) {
                Text(text = badge, style = MaterialTheme.typography.labelLarge, color = palette.teal)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HijriCalendarSourceSheetContentPreview() {
    SanguSantriTheme {
        Column {
            SourceSheetHeading(palette = hijriCalendarPalette())
            SourceBlock(
                title = stringResource(R.string.hijri_calendar_source_hijri_title),
                body = stringResource(R.string.hijri_calendar_source_hijri_body),
                badge = stringResource(R.string.hijri_calendar_calculation_status_umm_al_qura),
                palette = hijriCalendarPalette(),
            )
        }
    }
}
