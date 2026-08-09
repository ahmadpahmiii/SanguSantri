package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.HijriCalculationStatus
import com.sangusantri.app.domain.model.HijriCalendarEvent
import com.sangusantri.app.domain.model.HijriEventKind
import com.sangusantri.app.domain.model.HijriEventProvenance

/** One agenda item's provenance — CAL-FR-008: "Every production agenda item must expose its
 * source/status in the detail or source surface." Description is the curated bundle's own text,
 * never AI-generated at render time. */
@Composable
fun HijriCalendarEventDetailDialog(
    event: HijriCalendarEvent,
    onDismiss: () -> Unit,
) {
    val statusLabel =
        when (event.calculationStatus) {
            HijriCalculationStatus.UMM_AL_QURA_CALCULATION ->
                stringResource(
                    R.string.hijri_calendar_calculation_status_umm_al_qura,
                )
            HijriCalculationStatus.OFFICIAL_CONFIRMED ->
                stringResource(
                    R.string.hijri_calendar_calculation_status_official_confirmed,
                )
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = event.title) },
        text = {
            Column {
                if (event.description != null) {
                    Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = stringResource(R.string.hijri_calendar_event_detail_status_label, statusLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = SanguSantriSpacing.extraSmall),
                )
                Text(
                    text =
                        stringResource(
                            R.string.hijri_calendar_event_detail_source_label,
                            event.provenance.sourcePublisher,
                            event.provenance.sourceTitle,
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.hijri_calendar_event_detail_dismiss_action))
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun HijriCalendarEventDetailDialogPreview() {
    SanguSantriTheme {
        HijriCalendarEventDetailDialog(
            event =
                HijriCalendarEvent(
                    id = "ayyamul-bidh-1448",
                    kind = HijriEventKind.FASTING,
                    title = "Ayyamul Bidh",
                    description = "Puasa sunnah pertengahan bulan Hijriah (13–15).",
                    startDate = java.time.LocalDate.of(2026, 8, 26),
                    endDate = java.time.LocalDate.of(2026, 8, 28),
                    hijriYear = 1448,
                    hijriMonth = 3,
                    hijriStartDay = 13,
                    hijriEndDay = 15,
                    isFlexibleWindow = false,
                    calculationStatus = HijriCalculationStatus.UMM_AL_QURA_CALCULATION,
                    provenance =
                        HijriEventProvenance(
                            bundleVersion = 1,
                            sourcePublisher = "Kemenag Gorontalo",
                            sourceTitle = "Kakankemenag Sampaikan Hikmah Puasa dalam Safari Ramadan",
                            sourceUrl = "https://gorontalo.kemenag.go.id/…",
                            sourceYear = null,
                            editorialNote = "",
                        ),
                ),
            onDismiss = {},
        )
    }
}
