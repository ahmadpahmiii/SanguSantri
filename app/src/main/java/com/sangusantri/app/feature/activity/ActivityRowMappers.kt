package com.sangusantri.app.feature.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ActivityRowContent
import com.sangusantri.app.domain.model.AmaliyahCompletionEvent
import com.sangusantri.app.domain.model.QuranActivityEntry
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.feature.reminder.ReminderScheduleFormatter

@Composable
fun AmaliyahCompletionEvent.toRowContent(): ActivityRowContent = ActivityRowContent(
    primaryText = amaliyahTitleId,
    // `versionNumber` is deliberately not shown. It was the CMS's published edition number, which
    // meant something to a reader; it is now a purely local counter incremented whenever *this
    // device* noticed the steps change, so two phones show different numbers for the same
    // completion and a fresh install shows "Versi 1" for content corrected five times. The column
    // stays as an audit snapshot on the event row — dropping it is a Room schema change, and this
    // app's destructive-migration policy would wipe every user's history to remove one unused int.
    secondaryText = stringResource(R.string.activity_amaliyah_row_secondary, formatActivityDuration(durationMillis)),
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

/** `0.0.6`, standalone Al-Qur'an Kemenag — the title is fixed per
 * `docs/design/QURAN_DESIGN_SYSTEM.md` §5.9 ("Membaca Al-Qur'an"), never the surah name, so surah
 * and ayat range live in the secondary line instead. */
@Composable
fun QuranActivityEntry.toRowContent(): ActivityRowContent = ActivityRowContent(
    primaryText = stringResource(R.string.activity_quran_row_primary),
    secondaryText = stringResource(R.string.activity_quran_row_secondary, surahName, startAyat, endAyat),
    trailingText = formatActivityTime(readAtEpochMillis),
)

/** `0.0.4`, Pengingat Amaliyah. [Reminder.label] is pre-filled by the create form whenever a
 * preset is chosen, so a blank label here means a genuinely un-named custom reminder — falls back
 * to the raw content id rather than needing a `ContentRepository` lookup just for this row. */
@Composable
fun Reminder.toRowContent(hijriMonthNames: List<String>): ActivityRowContent = ActivityRowContent(
    primaryText = label.ifBlank { contentId },
    secondaryText = ReminderScheduleFormatter.formatScheduleSummary(schedule, hijriMonthNames),
    trailingText =
        stringResource(if (isEnabled) R.string.reminder_status_active else R.string.reminder_status_inactive),
)
