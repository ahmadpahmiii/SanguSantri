package com.sangusantri.app.feature.home

import androidx.annotation.StringRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.roundToInt

@Suppress("LongMethod", "LongParameterList")
/**
 * Beranda's strongest element (handoff §1.2, slimmed in turn 5): the one dark green panel on the
 * screen, and the entry point to the full schedule and kiblat. The whole surface is tappable.
 *
 * **Turn 5 shrank it to make room for the ayah header above.** The "Jadwal lengkap" chip went
 * because the five-time row right below it already carries that information and the block surface
 * already opens the schedule; kiblat moved out of the chip row into a pill in the header's
 * top-right, which is also the only thing in the block that does not open Jadwal Sholat's list.
 * The `mosque` icon and the chip row itself are gone with them.
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
    kiblatBearingDegrees: Float? = null,
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
                .padding(horizontal = BlockPaddingHorizontal, vertical = BlockPaddingVertical),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.beranda_prayer_next_label),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                    fontWeight = FontWeight.Bold,
                    color = BlockColors.dim,
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
                    modifier = Modifier.padding(top = HeadlineTopPadding),
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
                    modifier = Modifier.padding(top = SummaryTopPadding),
                )
            }
            KiblatPill(
                bearingDegrees = kiblatBearingDegrees,
                onClick = onOpenKiblat,
                modifier = Modifier.padding(start = SanguSantriSpacing.small),
            )
        }
        PrayerProgressLine(
            fraction = schedule.elapsedFractionAt(now),
            modifier = Modifier.padding(top = SanguSantriSpacing.medium),
        )
        PrayerTimesRow(
            schedule = schedule,
            highlighted = next.name,
            modifier = Modifier.padding(top = SanguSantriSpacing.medium),
        )
    }
}

/**
 * Handoff turn 5 §2 — kiblat's whole presence on Beranda: the direction, and a tap into the kiblat
 * section of Jadwal Sholat.
 *
 * **The `explore` icon is always here; the bearing is not.** The pill is the only route to kiblat
 * from this screen, so hiding it until a bearing exists hid the feature from exactly the people who
 * had not found it yet — and a bearing needs location permission, which most readers have not
 * granted on first run. With no bearing computed the pill shows the icon alone and opens the kiblat
 * card, where the compass explains itself and asks. What it must never do is show a number the app
 * has not computed.
 */
@Composable
private fun KiblatPill(
    bearingDegrees: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KiblatPillIconGap),
        modifier =
            modifier
                .height(KiblatPillHeight)
                .clip(RoundedCornerShape(KiblatPillHeight / 2))
                .background(BlockColors.chipBackground)
                .clickable(onClick = onClick)
                .padding(
                    horizontal =
                        if (bearingDegrees == null) KiblatPillIconOnlyPadding else KiblatPillHorizontalPadding,
                ),
    ) {
        Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = stringResource(R.string.beranda_prayer_kiblat),
            tint = BlockColors.chipText,
            modifier = Modifier.size(KiblatPillIconSize),
        )
        if (bearingDegrees != null) {
            Text(
                text = stringResource(R.string.beranda_prayer_kiblat_bearing, bearingDegrees.roundToInt()),
                fontSize = KiblatPillTextSize,
                color = BlockColors.chipText,
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

/**
 * All six entries across the panel's foot.
 *
 * Imsak is listed here even though it is not a prayer, because [highlighted] is the entry being
 * counted down to — the same one the headline above names — and between Isya and tomorrow's Imsak
 * that entry *is* Imsak. Reserving this row for the five prayers alone, as it was, left the whole
 * night with nothing marked.
 */
@Composable
private fun PrayerTimesRow(
    schedule: PrayerSchedule,
    highlighted: PrayerName,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            schedule.times.forEach { prayer ->
                val isNext = prayer.name == highlighted
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = prayer.name.label().uppercase(),
                        fontSize = TimesRowLabelSize,
                        letterSpacing = 0.5.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNext) BlockColors.strong else BlockColors.dim,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = prayer.time.formatAsClock(),
                        fontSize = TimesRowTimeSize,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNext) BlockColors.strong else BlockColors.dim,
                        modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall - 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrayerName.label(): String = stringResource(labelRes())

/** The same mapping outside a composition — the home-screen widget builds `RemoteViews`, which has
 * no `stringResource`, and a second copy of this `when` is exactly how the two would drift apart. */
@StringRes
internal fun PrayerName.labelRes(): Int =
    when (this) {
        PrayerName.IMSAK -> R.string.prayer_imsak
        PrayerName.SUBUH -> R.string.prayer_subuh
        PrayerName.ZUHUR -> R.string.prayer_zuhur
        PrayerName.ASAR -> R.string.prayer_asar
        PrayerName.MAGRIB -> R.string.prayer_magrib
        PrayerName.ISYA -> R.string.prayer_isya
    }

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
private val BlockCornerRadius = 20.dp
private val BlockPaddingVertical = 14.dp
private val BlockPaddingHorizontal = 15.dp
private val HeadlineTopPadding = 5.dp
private val SummaryTopPadding = 2.dp
private val ProgressLineHeight = 2.dp
private val PrayerHeadlineSize = 22.sp
private val KiblatPillHeight = 32.dp
private val KiblatPillHorizontalPadding = 12.dp

/** Icon alone wants an even inset, not the text pill's asymmetric one. */
private val KiblatPillIconOnlyPadding = 8.dp
private val KiblatPillIconGap = 5.dp
private val KiblatPillIconSize = 16.dp
private val KiblatPillTextSize = 12.sp
private val TimesRowLabelSize = 9.5.sp
private val TimesRowTimeSize = 12.sp

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
