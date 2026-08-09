package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.HijriCalendarPalette
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.core.designsystem.theme.hijriCalendarPalette
import com.sangusantri.app.domain.model.HijriCalendarEvent
import com.sangusantri.app.domain.model.HijriEventKind
import com.sangusantri.app.feature.hijricalendar.HijriCalendarAgendaFilter
import com.sangusantri.app.feature.hijricalendar.HijriCalendarAgendaFormatter

@Composable
fun HijriCalendarAgendaSection(
    events: List<HijriCalendarEvent>,
    filter: HijriCalendarAgendaFilter,
    onFilterSelected: (HijriCalendarAgendaFilter) -> Unit,
    onEventInfoClick: (HijriCalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = hijriCalendarPalette()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.hijri_calendar_agenda_title),
                style = MaterialTheme.typography.titleMedium,
            )
            AgendaLegend(palette = palette)
        }
        Row(
            modifier = Modifier.padding(top = SanguSantriSpacing.small, bottom = SanguSantriSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        ) {
            FilterChip(
                selected = filter == HijriCalendarAgendaFilter.ALL,
                onClick = { onFilterSelected(HijriCalendarAgendaFilter.ALL) },
                label = { Text(stringResource(R.string.hijri_calendar_filter_all)) },
            )
            FilterChip(
                selected = filter == HijriCalendarAgendaFilter.FASTING,
                onClick = { onFilterSelected(HijriCalendarAgendaFilter.FASTING) },
                label = { Text(stringResource(R.string.hijri_calendar_filter_fasting)) },
            )
            FilterChip(
                selected = filter == HijriCalendarAgendaFilter.HOLIDAY,
                onClick = { onFilterSelected(HijriCalendarAgendaFilter.HOLIDAY) },
                label = { Text(stringResource(R.string.hijri_calendar_filter_holiday)) },
            )
        }
        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.hijri_calendar_agenda_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(SanguSantriSpacing.default),
            )
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    events.forEachIndexed { index, event ->
                        HijriCalendarAgendaRow(
                            event = event,
                            palette = palette,
                            onInfoClick = { onEventInfoClick(event) },
                        )
                        if (index != events.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaLegend(palette: HijriCalendarPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        LegendItem(color = palette.amber, label = stringResource(R.string.hijri_calendar_agenda_legend_fasting))
        LegendItem(color = palette.coral, label = stringResource(R.string.hijri_calendar_agenda_legend_holiday))
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(percent = 50)).background(color))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HijriCalendarAgendaRow(
    event: HijriCalendarEvent,
    palette: HijriCalendarPalette,
    onInfoClick: () -> Unit,
) {
    val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
    val badgeColor = if (event.kind == HijriEventKind.FASTING) palette.amber else palette.coral
    Row(
        modifier = Modifier.fillMaxWidth().padding(SanguSantriSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(9.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = HijriCalendarAgendaFormatter.formatAgendaBadgeMonth(event.startDate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = HijriCalendarAgendaFormatter.formatAgendaBadgeDay(event),
                style = MaterialTheme.typography.titleMedium,
                color = badgeColor,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                text = HijriCalendarAgendaFormatter.formatEventHijriRange(event, hijriMonthNames),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AgendaStatusTag(kind = event.kind, palette = palette)
        }
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription =
                    stringResource(
                        R.string.hijri_calendar_agenda_row_source_content_description,
                        event.title,
                    ),
            )
        }
    }
}

@Composable
private fun AgendaStatusTag(
    kind: HijriEventKind,
    palette: HijriCalendarPalette,
) {
    val (labelRes, color, containerColor) =
        when (kind) {
            HijriEventKind.FASTING -> Triple(R.string.hijri_calendar_status_fasting, palette.amber, palette.amberSoft)
            HijriEventKind.FASTING_PROHIBITED ->
                Triple(
                    R.string.hijri_calendar_status_fasting_prohibited,
                    palette.coral,
                    palette.coralSoft,
                )
            HijriEventKind.RELIGIOUS_OBSERVANCE ->
                Triple(R.string.hijri_calendar_status_religious_observance, palette.coral, palette.coralSoft)
            HijriEventKind.NATIONAL_HOLIDAY ->
                Triple(
                    R.string.hijri_calendar_status_national_holiday,
                    palette.coral,
                    palette.coralSoft,
                )
            HijriEventKind.COLLECTIVE_LEAVE ->
                Triple(
                    R.string.hijri_calendar_status_collective_leave,
                    palette.coral,
                    palette.coralSoft,
                )
        }
    Box(
        modifier =
            Modifier
                .padding(top = 3.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(containerColor)
                .padding(horizontal = SanguSantriSpacing.small, vertical = 2.dp),
    ) {
        Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@PreviewLightDark
@Composable
private fun HijriCalendarAgendaSectionPreview() {
    SanguSantriTheme {
        HijriCalendarAgendaSection(
            events = emptyList(),
            filter = HijriCalendarAgendaFilter.ALL,
            onFilterSelected = {},
            onEventInfoClick = {},
        )
    }
}
