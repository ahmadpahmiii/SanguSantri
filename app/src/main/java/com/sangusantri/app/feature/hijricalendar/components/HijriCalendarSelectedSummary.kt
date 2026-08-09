package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.core.designsystem.theme.hijriCalendarPalette
import com.sangusantri.app.domain.model.HijriCalendarDay
import com.sangusantri.app.domain.model.HijriMonthGridCalculator
import com.sangusantri.app.feature.hijricalendar.HijriCalendarFormatter
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HijriCalendarSelectedSummary(
    day: HijriCalendarDay,
    modifier: Modifier = Modifier,
) {
    val palette = hijriCalendarPalette()
    val hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList()
    val weekdayNames = stringArrayResource(R.array.hijri_calendar_weekday_names).toList()
    val pasaranNames = stringArrayResource(R.array.hijri_calendar_pasaran_names).toList()

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = SanguSantriSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(palette.teal),
        )
        Column(modifier = Modifier.padding(start = SanguSantriSpacing.small)) {
            Text(
                text =
                    HijriCalendarFormatter.formatWeekdayAndPasaran(
                        day.date,
                        day.pasaran,
                        weekdayNames,
                        pasaranNames,
                    ),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = HijriCalendarFormatter.formatSelectedDateSubtitle(day, hijriMonthNames),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HijriCalendarSelectedSummaryPreview() {
    SanguSantriTheme {
        val month = HijriMonthGridCalculator.build(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 8))
        HijriCalendarSelectedSummary(day = month.days.first { it.date == LocalDate.of(2026, 8, 8) })
    }
}
