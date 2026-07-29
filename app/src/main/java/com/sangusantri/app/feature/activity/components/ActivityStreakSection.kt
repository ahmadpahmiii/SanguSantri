package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.SectionHeader
import com.sangusantri.app.core.designsystem.component.SummaryMetric
import com.sangusantri.app.core.designsystem.component.SummaryMetricEmphasis
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.ActivityOverview

/** Aktivitas' (`0.0.3`) "Ringkasan streak" section — no "Lihat semua" (a streak has no list). */
@Composable
fun ActivityStreakSection(
    overview: ActivityOverview,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.activity_section_streak))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
        ) {
            SummaryMetric(
                value = stringResource(R.string.activity_streak_days_value, overview.currentStreakDays),
                label = stringResource(R.string.activity_streak_current_label),
                emphasis = SummaryMetricEmphasis.HIGHLIGHTED,
            )
            SummaryMetric(
                value = stringResource(R.string.activity_streak_days_value, overview.longestStreakDays),
                label = stringResource(R.string.activity_streak_longest_label),
            )
        }
    }
}
