package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ActivityRow
import com.sangusantri.app.core.designsystem.component.ActivityRowContent
import com.sangusantri.app.core.designsystem.component.ActivityRowKind
import com.sangusantri.app.core.designsystem.component.SectionHeader

/**
 * A "Lihat semua"-fronted preview list — shared shape for Aktivitas' (`0.0.3`) amaliyah-completion
 * and tasbih-history sections. Only rendered by the caller when [rows] is non-empty (per-section
 * hide-if-empty rule); this component itself doesn't decide visibility.
 */
@Composable
fun ActivityHistorySection(
    title: String,
    kind: ActivityRowKind,
    rows: List<ActivityRowContent>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = title,
            actionLabel = stringResource(R.string.activity_see_all_action),
            onActionClick = onSeeAllClick,
        )
        rows.forEachIndexed { index, row ->
            ActivityRow(kind = kind, content = row, showDivider = index != rows.lastIndex)
        }
    }
}
