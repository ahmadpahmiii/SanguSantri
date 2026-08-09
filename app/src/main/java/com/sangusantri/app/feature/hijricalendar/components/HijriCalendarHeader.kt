package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.HijriCalendarMonth
import com.sangusantri.app.domain.model.HijriMonthGridCalculator
import com.sangusantri.app.feature.hijricalendar.HijriCalendarFormatter
import java.time.YearMonth

@Composable
fun HijriCalendarMonthHeader(
    month: HijriCalendarMonth,
    navigation: HijriCalendarMonthNavigation,
    modifier: Modifier = Modifier,
) {
    val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = SanguSantriSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = navigation.onPrevious, enabled = navigation.canGoToPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.hijri_calendar_previous_month_content_description),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = HijriCalendarFormatter.formatGregorianMonthYear(month.yearMonth),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text =
                    HijriCalendarFormatter.formatHijriMonthSpan(
                        month.hijriSpanStart,
                        month.hijriSpanEnd,
                        hijriMonthNames,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = navigation.onNext, enabled = navigation.canGoToNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.hijri_calendar_next_month_content_description),
            )
        }
    }
}

/** Full weekday names, Sunday-first, never abbreviated (PRD §7.1) — hidden from TalkBack since the
 * day cell's own content description already states the full weekday for each date. */
@Composable
fun HijriCalendarWeekdayRow(modifier: Modifier = Modifier) {
    val weekdayNames = stringArrayResource(R.array.hijri_calendar_weekday_names)
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayNames.forEach { name ->
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HijriCalendarMonthHeaderPreview() {
    SanguSantriTheme {
        Column {
            HijriCalendarMonthHeader(
                month = HijriMonthGridCalculator.build(YearMonth.of(2026, 8)),
                navigation =
                    HijriCalendarMonthNavigation(
                        canGoToPrevious = true,
                        canGoToNext = true,
                        onPrevious = {},
                        onNext = {},
                    ),
            )
            HijriCalendarWeekdayRow()
        }
    }
}
