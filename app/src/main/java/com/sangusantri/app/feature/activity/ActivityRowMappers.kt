package com.sangusantri.app.feature.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ActivityRowContent
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import com.sangusantri.app.domain.model.TasbihHistoryEntry

@Composable
fun AmaliyahCompletionEvent.toRowContent(): ActivityRowContent =
    ActivityRowContent(
        primaryText = amaliyahTitleId,
        secondaryText =
            stringResource(
                R.string.activity_amaliyah_row_secondary,
                versionNumber,
                formatActivityDuration(durationMillis),
            ),
        trailingText = formatActivityTime(completedAtEpochMillis),
    )

/** Reuses Tasbih's (0.0.2) own field-list strings — same underlying data, same wording. */
@Composable
fun TasbihHistoryEntry.toRowContent(): ActivityRowContent {
    val targetText = targetValue?.toString() ?: stringResource(R.string.tasbih_target_unlimited_short)
    val durationText = formatActivityDuration(endedAtEpochMillis - startedAtEpochMillis)
    return ActivityRowContent(
        primaryText = sessionName ?: stringResource(R.string.tasbih_history_row_name_default),
        secondaryText = stringResource(R.string.tasbih_history_row_target, targetText, finalCount),
        trailingText =
            stringResource(
                R.string.activity_row_trailing_time_and_duration,
                formatActivityTime(endedAtEpochMillis),
                durationText,
            ),
    )
}
