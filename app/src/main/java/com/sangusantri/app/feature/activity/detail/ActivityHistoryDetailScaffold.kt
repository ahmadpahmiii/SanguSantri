package com.sangusantri.app.feature.activity.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ActivityRow
import com.sangusantri.app.core.designsystem.component.ActivityRowContent
import com.sangusantri.app.core.designsystem.component.ActivityRowKind
import com.sangusantri.app.core.designsystem.component.TimeRangeFilterChips
import com.sangusantri.app.core.designsystem.component.TimeRangeFilterState
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * The shared "Lihat semua" list-screen shape (design spec states 6/7: back + title, plain
 * `Activity Row` list, no artificial page cap, a lightweight time-range filter) — one scaffold
 * reused by both the amaliyah and tasbih detail screens rather than two near-duplicate layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivityHistoryDetailScaffold(
    kind: ActivityRowKind,
    filterState: TimeRangeFilterState,
    rows: List<ActivityRowContent>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        when (kind) {
            ActivityRowKind.AMALIYAH -> stringResource(R.string.activity_detail_amaliyah_title)
            ActivityRowKind.TASBIH -> stringResource(R.string.activity_detail_tasbih_title)
        }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            TimeRangeFilterChips(
                selected = filterState.selected,
                onSelect = filterState.onSelect,
                modifier =
                    Modifier.padding(
                        horizontal = SanguSantriSpacing.default,
                        vertical = SanguSantriSpacing.small,
                    ),
            )
            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.activity_detail_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = SanguSantriSpacing.default)) {
                    itemsIndexed(rows) { index, row ->
                        ActivityRow(kind = kind, content = row, showDivider = index != rows.lastIndex)
                    }
                }
            }
        }
    }
}
