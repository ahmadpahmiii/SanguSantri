package com.sangusantri.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.feature.activity.components.AmalanWeekStrip
import java.time.LocalDate

/**
 * Beranda's one-line view of Amalan Harian, between the greeting row and the next-prayer block.
 *
 * Deliberately still rendered at zero, reading "Mulai hari ini": every other Beranda section hides
 * when it has nothing, but a goal that disappears until you have already met it can never start a
 * streak. One line, one fixed height, in every state.
 */
@Composable
fun BerandaAmalanPill(
    state: AmalanHarian,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hint =
        when {
            state.isTodayComplete && state.isUdzurToday -> stringResource(R.string.amalan_pill_complete_udzur)
            state.isTodayComplete -> stringResource(R.string.amalan_pill_complete)
            state.dzikirDone -> stringResource(R.string.amalan_pill_left_quran, state.quranPagesLeft)
            state.quranDone -> stringResource(R.string.amalan_pill_left_dzikir)
            else -> stringResource(R.string.amalan_pill_start_supporting)
        }
    val description = stringResource(R.string.amalan_pill_content_description)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PillCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = PillMinHeight)
                .clearAndSetSemantics { contentDescription = "$description $hint" },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            if (state.currentStreakDays > 0) {
                Text(
                    text = stringResource(R.string.amalan_streak_days_short, state.currentStreakDays),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                AmalanWeekStrip(week = state.week, showLabels = false)
            } else {
                Text(
                    text = stringResource(R.string.amalan_pill_start),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }
}

private val PillCornerRadius = 999.dp
private val PillMinHeight = 48.dp

@PreviewLightDark
@Composable
private fun BerandaAmalanPillPreview() {
    val today = LocalDate.of(2026, 8, 25)
    SanguSantriTheme {
        BerandaAmalanPill(
            state =
                AmalanHarian(
                    dzikirDone = true,
                    quranPagesToday = 2,
                    isUdzurToday = false,
                    currentStreakDays = 7,
                    longestStreakDays = 21,
                    week =
                        (6 downTo 0).map { back ->
                            AmalanDay(
                                date = today.minusDays(back.toLong()),
                                state = if (back == 0) AmalanDayState.PENDING else AmalanDayState.COMPLETE,
                            )
                        },
                    hasEverCompleted = true,
                ),
            onClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
