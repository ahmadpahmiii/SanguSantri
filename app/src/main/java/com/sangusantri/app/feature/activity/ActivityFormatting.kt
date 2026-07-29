package com.sangusantri.app.feature.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val activityTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatActivityTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(activityTimeFormatter)

/** Reuses Tasbih's (0.0.2) existing duration strings — the wording is generic, not Tasbih-specific. */
@Composable
fun formatActivityDuration(durationMillis: Long): String {
    val minutes = durationMillis / MILLIS_PER_MINUTE
    return if (minutes < 1) {
        stringResource(R.string.tasbih_history_row_duration_less_than_minute)
    } else {
        stringResource(R.string.tasbih_history_row_duration_minutes, minutes)
    }
}

private const val MILLIS_PER_MINUTE = 60_000L
