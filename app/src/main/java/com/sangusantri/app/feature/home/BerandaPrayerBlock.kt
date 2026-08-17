package com.sangusantri.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.BlockColors
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerSchedule
import java.time.LocalTime

@Suppress("LongMethod")
/**
 * Beranda's strongest element (handoff §1.2): the one dark green panel on the screen, and the entry
 * point to the full schedule and kiblat. The whole surface is tappable.
 *
 * Rendered only when a schedule exists — Beranda's standing rule that a section with no data is not
 * rendered. Until the user picks their city, or while the first fetch is pending, there is no
 * schedule and this block simply does not appear.
 */
@Composable
fun BerandaPrayerBlock(
    schedule: PrayerSchedule,
    now: LocalTime,
    onOpenSchedule: () -> Unit,
    onOpenKiblat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = schedule.nextAfter(now) ?: return
    val border = BlockColors.border
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BlockCornerRadius))
                .background(BlockColors.background)
                .then(
                    if (border != null) {
                        Modifier.border(BorderStroke(1.dp, border), RoundedCornerShape(BlockCornerRadius))
                    } else {
                        Modifier
                    },
                )
                .clickable(onClick = onOpenSchedule)
                .padding(BlockPadding),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.beranda_prayer_next_label),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                fontWeight = FontWeight.Bold,
                color = BlockColors.dim,
            )
            Icon(
                imageVector = Icons.Outlined.Mosque,
                contentDescription = null,
                tint = BlockColors.dim,
                modifier = Modifier.size(20.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            modifier = Modifier.padding(top = SanguSantriSpacing.small),
        ) {
            Text(
                text = next.name.label(),
                fontSize = PrayerHeadlineSize,
                fontWeight = FontWeight.Medium,
                color = BlockColors.strong,
            )
            Text(
                text = next.time.formatAsClock(),
                fontSize = PrayerHeadlineSize,
                fontWeight = FontWeight.Light,
                color = BlockColors.strong,
            )
            if (schedule.nextIsTomorrow(now)) {
                Text(
                    text = stringResource(R.string.beranda_prayer_tomorrow),
                    style = MaterialTheme.typography.bodySmall,
                    color = BlockColors.dim,
                )
            }
        }
        Text(
            text = schedule.remainingSummary(now),
            style = MaterialTheme.typography.bodySmall,
            color = BlockColors.text,
        )
        PrayerProgressLine(
            fraction = schedule.elapsedFractionAt(now),
            modifier = Modifier.padding(top = SanguSantriSpacing.medium),
        )
        PrayerTimesRow(
            schedule = schedule,
            highlighted = schedule.currentAt(now)?.name ?: next.name,
            modifier = Modifier.padding(top = SanguSantriSpacing.default),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            modifier = Modifier.padding(top = SanguSantriSpacing.default),
        ) {
            BlockChip(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.beranda_prayer_full_schedule),
                onClick = onOpenSchedule,
                modifier = Modifier.weight(1f),
            )
            BlockChip(
                icon = Icons.Outlined.Explore,
                label = stringResource(R.string.beranda_prayer_kiblat),
                onClick = onOpenKiblat,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PrayerProgressLine(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ProgressLineHeight)
                .clip(RoundedCornerShape(ProgressLineHeight))
                .background(BlockColors.track),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction)
                    .height(ProgressLineHeight)
                    .clip(RoundedCornerShape(ProgressLineHeight))
                    .background(BlockColors.fill),
        )
    }
}

/** The five daily prayers across the panel's foot; Imsak is part of the schedule but not of this
 * row, which the design reserves for the prayers themselves. */
@Composable
private fun PrayerTimesRow(
    schedule: PrayerSchedule,
    highlighted: PrayerName,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BlockColors.track),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SanguSantriSpacing.medium),
        ) {
            schedule.times
                .filter { it.name != PrayerName.IMSAK }
                .forEach { prayer ->
                    val isCurrent = prayer.name == highlighted
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = prayer.name.label().uppercase(),
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) BlockColors.strong else BlockColors.dim,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = prayer.time.formatAsClock(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) BlockColors.strong else BlockColors.dim,
                            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                        )
                    }
                }
        }
    }
}

@Composable
private fun BlockChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall, Alignment.CenterHorizontally),
        modifier =
            modifier
                .height(ChipHeight)
                .clip(RoundedCornerShape(ChipHeight / 2))
                .background(BlockColors.chipBackground)
                .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BlockColors.chipText,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = BlockColors.chipText,
        )
    }
}

@Composable
internal fun PrayerName.label(): String =
    stringResource(
        when (this) {
            PrayerName.IMSAK -> R.string.prayer_imsak
            PrayerName.SUBUH -> R.string.prayer_subuh
            PrayerName.ZUHUR -> R.string.prayer_zuhur
            PrayerName.ASAR -> R.string.prayer_asar
            PrayerName.MAGRIB -> R.string.prayer_magrib
            PrayerName.ISYA -> R.string.prayer_isya
        },
    )

/** Indonesian clock convention: a dot, not a colon. */
internal fun LocalTime.formatAsClock(): String = "%02d.%02d".format(hour, minute)

@Composable
private fun PrayerSchedule.remainingSummary(now: LocalTime): String {
    val remaining = remainingUntilNext(now)
    val place = location
    return if (remaining == null) {
        place
    } else {
        val hours = remaining.toHours()
        val minutes = remaining.toMinutes() % MINUTES_PER_HOUR
        val amount =
            if (hours > 0) {
                stringResource(R.string.beranda_prayer_remaining_hours_minutes, hours, minutes)
            } else {
                stringResource(R.string.beranda_prayer_remaining_minutes, minutes)
            }
        stringResource(R.string.beranda_prayer_remaining_summary, amount, place)
    }
}

private const val MINUTES_PER_HOUR = 60
private val BlockCornerRadius = 24.dp
private val BlockPadding = 18.dp
private val ProgressLineHeight = 2.dp
private val ChipHeight = 36.dp
private val PrayerHeadlineSize = 27.sp

/**
 * Shown in the block's place while no schedule exists yet.
 *
 * Beranda's rule is that a section with no data is not rendered, and that still holds for the
 * schedule itself — no times are invented here. But the block is also the only route to Jadwal
 * Sholat, so hiding it outright left the screen unreachable until a city was somehow chosen. This
 * is a setup affordance, not a fake schedule.
 */
@Composable
fun BerandaPrayerSetupRow(
    onOpenSchedule: () -> Unit,
    modifier: Modifier = Modifier,
    detecting: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(SetupRowCornerRadius))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onOpenSchedule)
                .padding(SetupRowPadding),
    ) {
        Icon(
            imageVector = Icons.Outlined.Mosque,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.beranda_prayer_setup_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text =
                    stringResource(
                        if (detecting) {
                            R.string.beranda_prayer_setup_detecting
                        } else {
                            R.string.beranda_prayer_setup_supporting
                        },
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private val SetupRowCornerRadius = 20.dp
private val SetupRowPadding = 16.dp
