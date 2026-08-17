package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.ActivityOverview

/** Aktivitas' (`0.0.3`) "Ringkasan streak" section — no "Lihat semua" (a streak has no list). */
@Composable
fun ActivityStreakSection(
    overview: ActivityOverview,
    modifier: Modifier = Modifier,
) {
    // Revamp handoff §9: one surface card with an outline, the streak as a large light number with
    // an inline unit, and the record sitting quietly opposite it. The design also shows seven
    // day dots; those need per-day completion data this overview does not carry, so they are left
    // out rather than approximated from the streak length.
    Surface(
        shape = RoundedCornerShape(StreakCardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(StreakCardPadding),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = overview.currentStreakDays.toString(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.activity_streak_days_unit),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = SanguSantriSpacing.extraSmall),
                    )
                }
                Text(
                    text = stringResource(R.string.activity_streak_current_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.activity_streak_record_label, overview.longestStreakDays),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val StreakCardCornerRadius = 24.dp
private val StreakCardPadding = 18.dp
