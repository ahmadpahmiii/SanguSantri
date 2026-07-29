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
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.ActivityOverview

/** Aktivitas' (`0.0.3`) "Ringkasan minggu ini" section — a rolling 7-day window, no "Lihat semua". */
@Composable
fun ActivityWeeklySection(
    overview: ActivityOverview,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.activity_section_weekly))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
        ) {
            SummaryMetric(
                value = overview.weeklyAmaliyahCompletedCount.toString(),
                label = stringResource(R.string.activity_weekly_amaliyah_label),
            )
            SummaryMetric(
                value = overview.weeklyTasbihSessionCount.toString(),
                label = stringResource(R.string.activity_weekly_tasbih_label),
            )
            SummaryMetric(
                value = overview.weeklyTotalMinutes.toString(),
                label = stringResource(R.string.activity_weekly_minutes_label),
            )
        }
    }
}
