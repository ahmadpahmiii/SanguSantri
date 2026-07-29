package com.sangusantri.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * A lightweight time-range filter for Aktivitas' (`0.0.3`) "Lihat semua" detail screens — a filter,
 * not a second navigation system (design spec's explicit distinction).
 */
enum class TimeRangeFilter { ALL, LAST_7_DAYS, LAST_30_DAYS }

@Composable
private fun TimeRangeFilter.label(): String =
    when (this) {
        TimeRangeFilter.ALL -> stringResource(R.string.activity_filter_all)
        TimeRangeFilter.LAST_7_DAYS -> stringResource(R.string.activity_filter_last_7_days)
        TimeRangeFilter.LAST_30_DAYS -> stringResource(R.string.activity_filter_last_30_days)
    }

@Composable
fun TimeRangeFilterChips(
    selected: TimeRangeFilter,
    onSelect: (TimeRangeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        TimeRangeFilter.entries.forEach { range ->
            FilterChip(
                selected = range == selected,
                onClick = { onSelect(range) },
                label = { Text(text = range.label()) },
            )
        }
    }
}

/** Applies [range] to a list of timestamped items — pure, no Compose/coroutine dependency. */
fun <T> List<T>.filterByTimeRange(
    range: TimeRangeFilter,
    nowEpochMillis: Long,
    timestampOf: (T) -> Long,
): List<T> =
    when (range) {
        TimeRangeFilter.ALL -> this
        TimeRangeFilter.LAST_7_DAYS -> filter { nowEpochMillis - timestampOf(it) <= MILLIS_7_DAYS }
        TimeRangeFilter.LAST_30_DAYS -> filter { nowEpochMillis - timestampOf(it) <= MILLIS_30_DAYS }
    }

private const val MILLIS_7_DAYS = 7L * 24 * 60 * 60 * 1000
private const val MILLIS_30_DAYS = 30L * 24 * 60 * 60 * 1000

@PreviewLightDark
@Composable
private fun TimeRangeFilterChipsPreview() {
    SanguSantriTheme {
        TimeRangeFilterChips(selected = TimeRangeFilter.ALL, onSelect = {})
    }
}
