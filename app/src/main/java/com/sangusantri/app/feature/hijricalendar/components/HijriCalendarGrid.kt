package com.sangusantri.app.feature.hijricalendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.HijriCalendarPalette
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.core.designsystem.theme.hijriCalendarPalette
import com.sangusantri.app.domain.model.HijriCalendarDay
import com.sangusantri.app.domain.model.HijriCalendarMonth
import com.sangusantri.app.domain.model.HijriMonthGridCalculator
import com.sangusantri.app.feature.hijricalendar.HijriCalendarFormatter
import java.time.LocalDate
import java.time.YearMonth

private val CellCornerRadius = 9.dp
private const val INACTIVE_ALPHA = 0.35f

/** Bundles the localised name arrays a day cell needs into one parameter. [weekdayNames] must be
 * Sunday-first ("Ahad") — see [HijriCalendarFormatter.formatWeekdayFull]. */
private data class HijriCalendarLocalizedNames(
    val hijriMonthNames: List<String>,
    val weekdayNames: List<String>,
    val pasaranNames: List<String>,
)

/** [palette] and [names] together, so [HijriCalendarDayCell]'s own parameter count stays under
 * detekt's threshold — every cell in a grid needs both, so bundling costs no flexibility. */
private data class HijriCalendarCellStyle(
    val palette: HijriCalendarPalette,
    val names: HijriCalendarLocalizedNames,
)

@Composable
fun HijriCalendarGrid(
    days: List<HijriCalendarDay>,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style =
        HijriCalendarCellStyle(
            palette = hijriCalendarPalette(),
            names =
                HijriCalendarLocalizedNames(
                    hijriMonthNames = stringArrayResource(R.array.hijri_month_names).toList(),
                    weekdayNames = stringArrayResource(R.array.hijri_calendar_weekday_names).toList(),
                    pasaranNames = stringArrayResource(R.array.hijri_calendar_pasaran_names).toList(),
                ),
        )
    val weeks = days.chunked(HijriCalendarMonth.WEEK_LENGTH)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
    ) {
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { day ->
                    HijriCalendarDayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        style = style,
                        onClick = { onDaySelected(day.date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HijriCalendarDayCell(
    day: HijriCalendarDay,
    isSelected: Boolean,
    style: HijriCalendarCellStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = style.palette
    val contentAlpha = if (day.isCurrentMonth) 1f else INACTIVE_ALPHA
    val numberColor =
        when {
            day.isDateNumberEmphasized -> palette.coral
            isSelected -> palette.teal
            day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    val cellShape = RoundedCornerShape(CellCornerRadius)
    val decoration =
        when {
            isSelected -> Modifier.background(palette.tealSoft).border(1.dp, palette.teal, cellShape)
            day.isToday -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, cellShape)
            else -> Modifier
        }
    val description = day.rememberContentDescription(isSelected, style.names)

    Box(
        modifier =
            modifier
                .padding(horizontal = 1.dp)
                .heightIn(min = SanguSantriDimensions.hijriCalendarDayCellMinHeight)
                .clip(cellShape)
                .then(decoration)
                .selectable(selected = isSelected, onClick = onClick)
                .semantics { contentDescription = description },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SanguSantriSpacing.extraSmall, bottom = SanguSantriSpacing.extraSmall / 2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = HijriCalendarFormatter.toArabicIndicDigits(day.hijriDay),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                modifier = Modifier.align(Alignment.End).padding(end = SanguSantriSpacing.extraSmall),
            )
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 15.sp,
                fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = numberColor.copy(alpha = contentAlpha),
            )
            Text(
                text = style.names.pasaranNames.getOrElse(day.pasaran.ordinal) { "" },
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            )
            EventDotsRow(day = day, palette = palette)
        }
    }
}

@Composable
private fun EventDotsRow(
    day: HijriCalendarDay,
    palette: HijriCalendarPalette,
) {
    val dotSize = SanguSantriDimensions.hijriCalendarEventDotSize / 2
    if (!day.hasFastingDot && !day.hasObservanceDot) {
        Box(Modifier.size(dotSize))
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        if (day.hasFastingDot) Dot(color = palette.amber, size = dotSize)
        if (day.hasObservanceDot) Dot(color = palette.coral, size = dotSize)
    }
}

@Composable
private fun Dot(
    color: Color,
    size: Dp,
) {
    Box(
        modifier =
            Modifier
                .padding(top = 2.dp)
                .size(size)
                .clip(RoundedCornerShape(percent = 50))
                .background(color),
    )
}

/** Matches the design's aria-label shape: "{weekday}, {gregorian}, {hijri}, {pasaran}" plus
 * today/selected/holiday/fasting suffixes (CAL-FR-010) — all through string resources, never
 * hardcoded, so TalkBack announces an ordinary localised Indonesian sentence. */
@Composable
private fun HijriCalendarDay.rememberContentDescription(
    isSelected: Boolean,
    names: HijriCalendarLocalizedNames,
): String {
    val weekday = HijriCalendarFormatter.formatWeekdayFull(date, names.weekdayNames)
    val gregorian = HijriCalendarFormatter.formatGregorianFull(date)
    val hijri = HijriCalendarFormatter.formatHijriFull(hijriYear, hijriMonth, hijriDay, names.hijriMonthNames)
    val pasaranName = names.pasaranNames.getOrElse(pasaran.ordinal) { "" }
    val base = stringResource(R.string.hijri_calendar_day_cell_description, weekday, gregorian, hijri, pasaranName)
    val todaySuffix = if (isToday) stringResource(R.string.hijri_calendar_day_cell_today_suffix) else ""
    val selectedSuffix = if (isSelected) stringResource(R.string.hijri_calendar_day_cell_selected_suffix) else ""
    val holidaySuffix =
        if (isOfficialHoliday) {
            stringResource(
                R.string.hijri_calendar_day_cell_official_holiday_suffix,
            )
        } else {
            ""
        }
    val fastingSuffix = if (hasFastingDot) stringResource(R.string.hijri_calendar_day_cell_fasting_suffix) else ""
    return base + todaySuffix + selectedSuffix + holidaySuffix + fastingSuffix
}

@PreviewLightDark
@Composable
private fun HijriCalendarGridPreview() {
    SanguSantriTheme {
        val yearMonth = YearMonth.of(2026, 8)
        val today = LocalDate.of(2026, 8, 8)
        val month = HijriMonthGridCalculator.build(yearMonth, today)
        HijriCalendarGrid(days = month.days, selectedDate = today, onDaySelected = {})
    }
}
