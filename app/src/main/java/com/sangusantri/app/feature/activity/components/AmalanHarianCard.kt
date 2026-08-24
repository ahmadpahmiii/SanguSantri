package com.sangusantri.app.feature.activity.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import java.time.LocalDate

/**
 * Aktivitas' first block: today's two targets, the streak they feed, and the seven-day strip
 * (`docs/design/STREAK_GAMIFICATION_CONCEPT.md`).
 *
 * It replaces the old streak box in the same slot and with the same shape, so nothing above or
 * below it moves. Both targets must land for a day to count; during udzur the Qur'an row is shown
 * suspended rather than unmet, because a paused target is not a failed one.
 */
@Composable
fun AmalanHarianCard(
    state: AmalanHarian,
    onDzikirClick: () -> Unit,
    onQuranClick: () -> Unit,
    onUdzurChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                1.dp,
                if (state.isTodayComplete) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
            ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(CardPadding),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.amalan_title),
                style = MaterialTheme.typography.titleMedium,
            )
            AmalanHeadline(state)
            AmalanGoalRows(state = state, onDzikirClick = onDzikirClick, onQuranClick = onQuranClick)
            if (state.currentStreakDays > 0 || state.hasEverCompleted) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AmalanStreakRow(state)
                AmalanWeekStrip(state.week)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            AmalanUdzurRow(isUdzur = state.isUdzurToday, onUdzurChange = onUdzurChange)
        }
    }
}

/** One line that says either what is left, that the day is done, or that udzur is carrying it. */
@Composable
private fun AmalanHeadline(state: AmalanHarian) {
    if (state.isUdzurToday) {
        Surface(
            shape = RoundedCornerShape(BannerCornerRadius),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.amalan_udzur_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier =
                    Modifier.padding(
                        horizontal = SanguSantriSpacing.medium,
                        vertical = SanguSantriSpacing.small,
                    ),
            )
        }
        return
    }
    val text =
        when {
            state.isTodayComplete -> stringResource(R.string.amalan_complete)
            !state.hasEverCompleted && state.doneCount == 0 -> stringResource(R.string.amalan_first_run)
            state.dzikirDone ->
                stringResource(R.string.amalan_progress_left_quran, state.doneCount, state.quranPagesLeft)

            state.quranDone -> stringResource(R.string.amalan_progress_left_dzikir)
            else -> stringResource(R.string.amalan_progress, state.doneCount, state.targetCount)
        }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color =
            if (state.isTodayComplete) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    )
}

@Composable
private fun AmalanGoalRows(
    state: AmalanHarian,
    onDzikirClick: () -> Unit,
    onQuranClick: () -> Unit,
) {
    val quranTarget = AmalanHarian.QURAN_PAGE_TARGET
    val dzikirValue =
        stringResource(if (state.dzikirDone) R.string.amalan_value_done else R.string.amalan_value_pending)
    val quranValue =
        when {
            state.isUdzurToday -> stringResource(R.string.amalan_value_paused)
            else -> stringResource(R.string.amalan_value_pages, state.quranPagesToday, quranTarget)
        }
    Column {
        AmalanGoalRow(
            title = stringResource(R.string.amalan_row_dzikir),
            target = stringResource(R.string.amalan_row_dzikir_target),
            value = dzikirValue,
            progress = if (state.dzikirDone) 1f else 0f,
            done = state.dzikirDone,
            paused = false,
            contentDescription = stringResource(R.string.amalan_row_dzikir_content_description, dzikirValue),
            onClick = onDzikirClick,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        AmalanGoalRow(
            title = stringResource(R.string.amalan_row_quran),
            target = stringResource(R.string.amalan_row_quran_target, quranTarget),
            value = quranValue,
            progress = (state.quranPagesToday.toFloat() / quranTarget).coerceIn(0f, 1f),
            done = state.quranDone,
            paused = state.isUdzurToday,
            contentDescription = stringResource(R.string.amalan_row_quran_content_description, quranValue),
            onClick = onQuranClick,
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun AmalanGoalRow(
    title: String,
    target: String,
    value: String,
    progress: Float,
    done: Boolean,
    paused: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                .semantics { this.contentDescription = contentDescription },
    ) {
        AmalanTick(progress = progress, done = done, paused = paused)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = target,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Empty ring, ring filled to [progress], solid when done — never colour alone. */
@Composable
private fun AmalanTick(
    progress: Float,
    done: Boolean,
    paused: Boolean,
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier =
            Modifier
                .size(TickSize)
                .drawBehind {
                    val stroke = TickStroke.toPx()
                    val inset = stroke / 2
                    drawCircle(
                        color = if (paused) outline.copy(alpha = PAUSED_ALPHA) else outline,
                        radius = size.minDimension / 2 - inset,
                        style = Stroke(width = stroke),
                    )
                    if (done) {
                        drawCircle(color = primary, radius = size.minDimension / 2 - inset)
                    } else if (progress > 0f) {
                        drawArc(
                            color = primary,
                            startAngle = ARC_START_ANGLE,
                            sweepAngle = FULL_SWEEP * progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                },
    )
}

@Composable
private fun AmalanStreakRow(state: AmalanHarian) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.currentStreakDays.toString(),
                fontSize = StreakNumberSize,
                fontWeight = FontWeight.Light,
                color =
                    if (state.currentStreakDays > 0) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Text(
                text =
                    stringResource(
                        if (state.currentStreakDays > 0) {
                            R.string.amalan_streak_unit_long
                        } else {
                            R.string.amalan_streak_unit
                        },
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = SanguSantriSpacing.extraSmall, bottom = 3.dp),
            )
        }
        Text(
            text = stringResource(R.string.amalan_streak_record, state.longestStreakDays),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (state.currentStreakDays == 0 && state.longestStreakDays > 0) {
        Text(
            text = stringResource(R.string.amalan_streak_restart, state.longestStreakDays),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AmalanUdzurRow(
    isUdzur: Boolean,
    onUdzurChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.amalan_udzur_toggle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(checked = isUdzur, onCheckedChange = onUdzurChange)
    }
}

private val CardCornerRadius = 24.dp
private val CardPadding = 18.dp
private val BannerCornerRadius = 12.dp
private val TickSize = 20.dp
private val TickStroke = 1.5.dp
private val StreakNumberSize = 34.sp
private const val PAUSED_ALPHA = 0.5f
private const val ARC_START_ANGLE = -90f
private const val FULL_SWEEP = 360f

private fun previewWeek(states: List<AmalanDayState>): List<AmalanDay> {
    val today = LocalDate.of(2026, 8, 25)
    return states.mapIndexed { index, state ->
        AmalanDay(date = today.minusDays((states.size - 1 - index).toLong()), state = state)
    }
}

@PreviewLightDark
@Composable
private fun AmalanHarianCardInProgressPreview() {
    SanguSantriTheme {
        AmalanHarianCard(
            state =
                AmalanHarian(
                    dzikirDone = true,
                    quranPagesToday = 2,
                    isUdzurToday = false,
                    currentStreakDays = 7,
                    longestStreakDays = 21,
                    week = previewWeek(List(6) { AmalanDayState.COMPLETE } + AmalanDayState.PENDING),
                    hasEverCompleted = true,
                ),
            onDzikirClick = {},
            onQuranClick = {},
            onUdzurChange = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}

@PreviewLightDark
@Composable
private fun AmalanHarianCardUdzurPreview() {
    SanguSantriTheme {
        AmalanHarianCard(
            state =
                AmalanHarian(
                    dzikirDone = true,
                    quranPagesToday = 0,
                    isUdzurToday = true,
                    currentStreakDays = 12,
                    longestStreakDays = 21,
                    week =
                        previewWeek(
                            listOf(
                                AmalanDayState.COMPLETE,
                                AmalanDayState.UDZUR,
                                AmalanDayState.UDZUR,
                                AmalanDayState.UDZUR,
                                AmalanDayState.COMPLETE,
                                AmalanDayState.COMPLETE,
                                AmalanDayState.COMPLETE,
                            ),
                        ),
                    hasEverCompleted = true,
                ),
            onDzikirClick = {},
            onQuranClick = {},
            onUdzurChange = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
