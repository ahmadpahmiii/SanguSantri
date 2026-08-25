package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState

/**
 * Seven dots, oldest first. One semantics node for the whole row, not seven bare circles.
 *
 * [showLabels] is off on Beranda, where the pill is a single fixed-height line and the day letters
 * would force it to two.
 */
@Composable
fun AmalanWeekStrip(
    week: List<AmalanDay>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
) {
    val completed = week.count { it.state == AmalanDayState.COMPLETE }
    val description = stringResource(R.string.amalan_week_content_description, completed)
    val labels =
        listOf(
            R.string.amalan_day_monday,
            R.string.amalan_day_tuesday,
            R.string.amalan_day_wednesday,
            R.string.amalan_day_thursday,
            R.string.amalan_day_friday,
            R.string.amalan_day_saturday,
            R.string.amalan_day_sunday,
        )
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DotGap)) {
            week.forEach { day -> AmalanDayDot(day.state) }
        }
        if (showLabels) {
            Row(horizontalArrangement = Arrangement.spacedBy(DotGap)) {
                week.forEach { day ->
                    Text(
                        // DayOfWeek is 1..7 Monday-first, matching the label order above.
                        text = stringResource(labels[day.date.dayOfWeek.value - 1]),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(width = DotSize, height = DayLabelHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun AmalanDayDot(state: AmalanDayState) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val tertiary = MaterialTheme.colorScheme.tertiary
    when (state) {
        AmalanDayState.COMPLETE ->
            Box(
                modifier =
                    Modifier
                        .size(DotSize)
                        .background(primary, CircleShape),
            )

        AmalanDayState.INCOMPLETE ->
            Box(
                modifier =
                    Modifier
                        .size(DotSize)
                        .border(DotStroke, outline, CircleShape),
            )

        // Dashed rings are not a Compose primitive at this size; a half-alpha ring reads as
        // "paused" and still differs from both filled and plain-outline in greyscale.
        AmalanDayState.UDZUR ->
            Box(
                modifier =
                    Modifier
                        .size(DotSize)
                        .border(DotStroke, outline.copy(alpha = PAUSED_ALPHA), CircleShape)
                        .background(outline.copy(alpha = UDZUR_FILL_ALPHA), CircleShape),
            )

        AmalanDayState.PENDING ->
            Box(
                modifier =
                    Modifier
                        .size(DotSize)
                        .border(TodayStroke, tertiary, CircleShape),
            )
    }
}

private val DotSize = 12.dp
private val DotGap = 10.dp
private val DotStroke = 1.5.dp
private val TodayStroke = 2.dp
private val DayLabelHeight = 14.dp
private const val PAUSED_ALPHA = 0.5f
private const val UDZUR_FILL_ALPHA = 0.15f
