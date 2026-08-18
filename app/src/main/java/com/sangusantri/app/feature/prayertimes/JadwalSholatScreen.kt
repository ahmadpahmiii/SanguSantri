@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.prayertimes

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.BlockColors
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.PrayerTime
import com.sangusantri.app.feature.home.formatAsClock
import com.sangusantri.app.feature.home.label
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

@Composable
fun JadwalSholatRoute(
    onBack: () -> Unit,
    viewModel: JadwalSholatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Coarse location is requested only when the reader asks for the kiblat bearing, never on
    // open — the schedule itself needs no location at all.
    // The same permission serves two purposes here — the bearing, and filling the city in from
    // where the reader is. This remembers which one asked, so granting does the thing they wanted.
    var kiblatRequested by remember { mutableStateOf(true) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) return@rememberLauncherForActivityResult
            if (kiblatRequested) viewModel.refreshKiblat() else viewModel.detectCity()
        }
    JadwalSholatScreen(
        uiState = uiState,
        onBack = onBack,
        actions =
            JadwalSholatActions(
                onNotificationToggle = viewModel::setNotificationEnabled,
                onOpenCityPicker = viewModel::openCityPicker,
                onDismissCityPicker = viewModel::dismissCityPicker,
                onCityQueryChanged = viewModel::updateCityQuery,
                onCitySelected = viewModel::selectCity,
                onRequestKiblat = {
                    kiblatRequested = true
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                onDetectLocation = {
                    kiblatRequested = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                onRetry = viewModel::refreshSchedule,
            ),
    )
}

/** [JadwalSholatScreen]'s callbacks, bundled to keep its parameter list short (same pattern as
 * `SerambiActions`/`QuranHubActions`). */
data class JadwalSholatActions(
    val onNotificationToggle: (PrayerName, Boolean) -> Unit,
    val onOpenCityPicker: () -> Unit,
    val onDismissCityPicker: () -> Unit,
    val onCityQueryChanged: (String) -> Unit,
    val onCitySelected: (String) -> Unit,
    val onRequestKiblat: () -> Unit,
    val onDetectLocation: () -> Unit,
    val onRetry: () -> Unit,
)

@Suppress("LongMethod")
/**
 * Jadwal Sholat + Kiblat (handoff §2). Countdown block, the day's six rows with independent
 * per-prayer reminder toggles, and the kiblat card — kiblat has no screen of its own by design.
 *
 * With no prayer-time source wired, the screen states that plainly instead of rendering a schedule.
 * The compass likewise draws no needle without a real bearing: a needle pointing at nothing would be
 * worse than no needle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalSholatScreen(
    uiState: JadwalSholatUiState,
    onBack: () -> Unit,
    actions: JadwalSholatActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.jadwal_sholat_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        uiState.selectedCity?.let { city ->
                            Text(
                                text =
                                    stringResource(
                                        R.string.jadwal_sholat_subtitle,
                                        city.name,
                                        uiState.schedule?.source ?: stringResource(R.string.jadwal_sholat_source),
                                    ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = actions.onOpenCityPicker) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = stringResource(R.string.jadwal_sholat_change_city),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = SanguSantriSpacing.large),
        ) {
            val schedule = uiState.schedule
            if (schedule == null) {
                JadwalSholatUnavailable(
                    hasCity = uiState.selectedCity != null,
                    error = uiState.errorMessage,
                    isRefreshing = uiState.isRefreshing,
                    onChooseCity = actions.onOpenCityPicker,
                    onDetectLocation = actions.onDetectLocation,
                    onRetry = actions.onRetry,
                )
            } else {
                CountdownBlock(
                    schedule = schedule,
                    now = uiState.now,
                    today = uiState.today,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
                PrayerRows(
                    schedule = schedule,
                    now = uiState.now,
                    onNotificationToggle = actions.onNotificationToggle,
                    modifier = Modifier.padding(top = SectionGap),
                )
            }
            KiblatCard(
                bearing = uiState.kiblatBearing,
                onRequestKiblat = actions.onRequestKiblat,
                modifier =
                    Modifier.padding(
                        start = ScreenPadding,
                        end = ScreenPadding,
                        top = SectionGap,
                    ),
            )
        }
    }
    if (uiState.cityPickerVisible) {
        CityPickerSheet(uiState = uiState, actions = actions)
    }
}

/** Three different nothings, and the difference matters to the reader: no city chosen yet, a fetch
 * that failed with nothing cached, or a fetch still in flight. */
@Composable
@Suppress("LongParameterList")
private fun JadwalSholatUnavailable(
    hasCity: Boolean,
    error: PrayerScheduleError?,
    isRefreshing: Boolean,
    onChooseCity: () -> Unit,
    onDetectLocation: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = SectionGap),
    ) {
        if (isRefreshing) {
            CircularProgressIndicator()
            return@Column
        }
        val unreachable = error == PrayerScheduleError.OFFLINE_OR_UNREACHABLE
        Text(
            text =
                stringResource(
                    if (hasCity) R.string.jadwal_sholat_unavailable_title else R.string.jadwal_sholat_choose_city_title,
                ),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text =
                stringResource(
                    when {
                        error == PrayerScheduleError.CITY_DETECTION_FAILED -> R.string.jadwal_sholat_detect_failed
                        !hasCity -> R.string.jadwal_sholat_choose_city_description
                        unreachable -> R.string.jadwal_sholat_offline_description
                        else -> R.string.jadwal_sholat_unavailable_description
                    },
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SanguSantriSpacing.small),
        )
        Button(
            onClick = if (hasCity) onRetry else onChooseCity,
            modifier = Modifier.padding(top = SanguSantriSpacing.default),
        ) {
            Text(
                text =
                    stringResource(
                        if (hasCity) R.string.jadwal_sholat_retry else R.string.jadwal_sholat_choose_city_action,
                    ),
            )
        }
        // The second path the design offers: let location fill the city in instead of picking it.
        if (!hasCity) {
            TextButton(onClick = onDetectLocation) {
                Text(text = stringResource(R.string.jadwal_sholat_detect_location_action))
            }
        }
    }
}

@Composable
private fun CountdownBlock(
    schedule: PrayerSchedule,
    now: LocalTime,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val next = schedule.nextAfter(now) ?: return
    val remaining = schedule.remainingUntilNext(now)
    val border = BlockColors.border
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
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
                .padding(BlockPadding),
    ) {
        Text(
            text = stringResource(R.string.jadwal_sholat_countdown_label, next.name.label().uppercase()),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            fontWeight = FontWeight.Bold,
            color = BlockColors.dim,
        )
        Text(
            text = remaining.formatCountdown(),
            fontSize = CountdownSize,
            fontWeight = FontWeight.Light,
            letterSpacing = (-1).sp,
            color = BlockColors.strong,
            modifier = Modifier.padding(top = SanguSantriSpacing.small),
        )
        // Gregorian + hijri, from the app's own offline HijrahDate — the prayer-times source also
        // publishes a hijri calendar, but taking it would make this line network-dependent for
        // something the app already computes correctly offline.
        Text(
            text = today.formatWithHijri(stringArrayResource(R.array.hijri_month_names).toList()),
            style = MaterialTheme.typography.bodySmall,
            color = BlockColors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
        )
    }
}

@Composable
private fun PrayerRows(
    schedule: PrayerSchedule,
    now: LocalTime,
    onNotificationToggle: (PrayerName, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = schedule.currentAt(now)?.name
    Column(modifier = modifier.fillMaxWidth()) {
        schedule.times.forEach { prayer ->
            PrayerRow(
                prayer = prayer,
                isCurrent = prayer.name == current,
                onNotificationToggle = { onNotificationToggle(prayer.name, it) },
            )
            if (prayer.name != current) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
            }
        }
    }
}

@Suppress("LongMethod")
/** The current prayer's row is a filled tint block bled into the screen padding (handoff §2) — the
 * one place on this screen where a row leaves the hairline rhythm. */
@Composable
private fun PrayerRow(
    prayer: PrayerTime,
    isCurrent: Boolean,
    onNotificationToggle: (Boolean) -> Unit,
) {
    val rowModifier =
        if (isCurrent) {
            Modifier
                .padding(horizontal = ScreenPadding - CurrentRowBleed)
                .clip(RoundedCornerShape(CurrentRowCornerRadius))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = CurrentRowBleed)
        } else {
            Modifier.padding(horizontal = ScreenPadding)
        }
    val leadingColor =
        if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val nameColor =
        if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
    val rowFontSize = if (isCurrent) 16.sp else 15.sp
    val rowWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(rowModifier)
                .padding(vertical = SanguSantriSpacing.medium),
    ) {
        Icon(
            imageVector = prayer.name.icon(),
            contentDescription = null,
            tint = leadingColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = prayer.name.label(),
            fontSize = rowFontSize,
            fontWeight = rowWeight,
            color = nameColor,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = SanguSantriSpacing.medium),
        )
        Text(
            text = prayer.time.formatAsClock(),
            fontSize = rowFontSize,
            fontWeight = rowWeight,
            color = leadingColor,
        )
        IconButton(onClick = { onNotificationToggle(!prayer.notificationEnabled) }) {
            Icon(
                imageVector =
                    if (prayer.notificationEnabled) {
                        Icons.Outlined.NotificationsActive
                    } else {
                        Icons.Outlined.NotificationsOff
                    },
                contentDescription =
                    stringResource(
                        if (prayer.notificationEnabled) {
                            R.string.jadwal_sholat_notification_on
                        } else {
                            R.string.jadwal_sholat_notification_off
                        },
                        prayer.name.label(),
                    ),
                tint =
                    if (prayer.notificationEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Suppress("LongMethod")
/**
 * Handoff §2's kiblat card. The compass face is drawn, but the needle only appears once a real
 * bearing exists — with no location permission and no coordinates there is nothing to point at, and
 * a needle at an arbitrary angle would be worse than none.
 */
@Composable
private fun KiblatCard(
    bearing: Float?,
    onRequestKiblat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Auto-calibrating: the heading is read live from the device, so the needle tracks the phone
    // without the reader doing anything. Null on a device with no rotation-vector sensor.
    val heading = rememberDeviceHeading()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BlockCornerRadius))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(BlockCornerRadius))
                // A cached bearing is wrong as soon as the reader travels, so the card stays
                // tappable to recompute rather than being a one-time setup.
                .clickable(onClick = onRequestKiblat)
                .padding(CardPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.kiblat_card_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (bearing != null) {
                Text(
                    text = stringResource(R.string.kiblat_recompute_action),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
            modifier = Modifier.padding(top = SanguSantriSpacing.medium),
        ) {
            CompassFace(bearing = bearing, heading = heading)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        bearing
                            ?.let { stringResource(R.string.kiblat_bearing_degrees, it.toInt()) }
                            ?: stringResource(R.string.kiblat_bearing_unknown),
                    fontSize = if (bearing != null) BearingSize else 20.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text =
                        stringResource(
                            when {
                                bearing == null -> R.string.kiblat_unavailable_description
                                heading == null -> R.string.kiblat_calibration_hint
                                heading.needsCalibration -> R.string.kiblat_calibrate_hint
                                else -> R.string.kiblat_heading_hint
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                )
                if (bearing == null) {
                    TextButton(
                        onClick = onRequestKiblat,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(R.string.kiblat_enable_action))
                    }
                }
            }
        }
    }
}

private fun PrayerName.icon(): ImageVector =
    when (this) {
        PrayerName.IMSAK -> Icons.Outlined.WbTwilight
        PrayerName.SUBUH -> Icons.Outlined.Brightness5
        PrayerName.ZUHUR -> Icons.Outlined.LightMode
        PrayerName.ASAR -> Icons.Outlined.Brightness5
        PrayerName.MAGRIB -> Icons.Outlined.WbTwilight
        PrayerName.ISYA -> Icons.Outlined.Bedtime
    }

private fun Duration?.formatCountdown(): String {
    if (this == null) return "--.--.--"
    return "%d.%02d.%02d".format(toHours(), toMinutes() % 60, seconds % 60)
}

/**
 * A live qibla compass. The needle points at the qibla **relative to how the phone is currently
 * held** — `qibla - deviceHeading` — so turning the device turns the needle, which is what people
 * expect from a kiblat and what makes it usable rather than decorative.
 *
 * Without a rotation-vector sensor (many emulators, some tablets) it falls back to the absolute
 * bearing from north and says so, rather than leaving a needle that never moves.
 *
 * The rotation is animated, and the animation crosses 0°/360° the short way — otherwise the needle
 * whips a full turn every time the reader walks past north.
 */
@Composable
private fun CompassFace(
    bearing: Float?,
    heading: DeviceHeading?,
) {
    val needleColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val target = if (bearing == null) null else (bearing - (heading?.azimuthDegrees ?: 0f)).normalizeDegrees()
    val animated = rememberShortestPathAngle(target)

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(CompassSize)
                .clip(RoundedCornerShape(CompassSize / 2))
                .border(1.dp, outlineColor, RoundedCornerShape(CompassSize / 2)),
    ) {
        Text(
            text = stringResource(R.string.kiblat_compass_north),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = SanguSantriSpacing.small),
        )
        if (animated != null) {
            Canvas(modifier = Modifier.size(CompassSize)) {
                val centre = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - NeedleInset.toPx()
                // Degrees clockwise from the top of the dial; screen y grows downward, so north is -y.
                val radians = Math.toRadians(animated.toDouble())
                val tip =
                    Offset(
                        x = centre.x + (radius * kotlin.math.sin(radians)).toFloat(),
                        y = centre.y - (radius * kotlin.math.cos(radians)).toFloat(),
                    )
                drawLine(
                    color = needleColor,
                    start = centre,
                    end = tip,
                    strokeWidth = NeedleStroke.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(color = needleColor, radius = NeedleHubRadius.toPx(), center = centre)
            }
        }
    }
}

/** Animates to [target] the short way round the dial. Feeding a raw 359°→1° step to
 * `animateFloatAsState` sweeps the needle backwards through the whole circle; accumulating the
 * wrapped delta instead keeps the motion continuous. */
@Composable
private fun rememberShortestPathAngle(target: Float?): Float? {
    if (target == null) return null
    var continuous by remember { mutableFloatStateOf(target) }
    LaunchedEffect(target) {
        val delta = ((target - continuous + HALF_TURN) % FULL_TURN + FULL_TURN) % FULL_TURN - HALF_TURN
        continuous += delta
    }
    val animated by animateFloatAsState(targetValue = continuous, label = "kiblat needle")
    return animated
}

/**
 * City selection. Prayer times are keyed by kabupaten/kota, so this is the whole of what the
 * schedule needs to know about where the reader is — no location permission involved.
 *
 * Design notes, since a 517-row list is easy to get wrong:
 * - opens fully expanded, because scanning a long alphabetical list in a half sheet is miserable;
 * - the search field takes focus immediately — the list starts at "KAB. ACEH BARAT" and nobody
 *   scrolls to "KUDUS" by hand;
 * - the currently selected city is pinned above the results with a check, so the sheet always
 *   answers "which one am I on?" without searching for it;
 * - `imePadding` keeps the results above the keyboard rather than behind it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityPickerSheet(
    uiState: JadwalSholatUiState,
    actions: JadwalSholatActions,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = actions.onDismissCityPicker,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape =
            RoundedCornerShape(
                topStart = SanguSantriDimensions.sheetTopCornerRadius,
                topEnd = SanguSantriDimensions.sheetTopCornerRadius,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .imePadding(),
        ) {
            CityPickerHeader(onDismiss = actions.onDismissCityPicker)
            CitySearchField(
                query = uiState.cityQuery,
                onQueryChange = actions.onCityQueryChanged,
                focusRequester = focusRequester,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            CityPickerResults(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun CityPickerHeader(onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = ScreenPadding, end = SanguSantriSpacing.small, bottom = SanguSantriSpacing.medium),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.jadwal_sholat_choose_city_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.jadwal_sholat_city_sheet_supporting),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.jadwal_sholat_close_city_picker),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A filled tint field rather than an outlined one: the sheet already sits on a surface, and the
 * design's search inputs are filled. Explicit 16dp corners — the theme's default text-field shape
 * inherits the same pill radius that domed the sheet. */
@Composable
private fun CitySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = RoundedCornerShape(SearchFieldCornerRadius),
        placeholder = {
            Text(
                text = stringResource(R.string.jadwal_sholat_city_search_hint),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.jadwal_sholat_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding)
                .padding(bottom = SanguSantriSpacing.medium)
                .focusRequester(focusRequester),
    )
}

@Composable
private fun CityPickerResults(
    uiState: JadwalSholatUiState,
    actions: JadwalSholatActions,
) {
    val selected = uiState.selectedCity
    // Pinned only while browsing: during a search the reader is looking for something else, and a
    // sticky row unrelated to the query is just noise.
    val pinned = selected?.takeIf { uiState.cityQuery.isBlank() }
    val results = uiState.cities.filterNot { pinned != null && it.id == pinned.id }

    when {
        uiState.isRefreshing && uiState.cities.isEmpty() ->
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(SectionGap),
            ) { CircularProgressIndicator() }

        uiState.errorMessage == PrayerScheduleError.CITY_LIST_UNAVAILABLE && uiState.cities.isEmpty() ->
            CityPickerMessage(stringResource(R.string.jadwal_sholat_city_list_unavailable))

        uiState.cities.isEmpty() ->
            CityPickerMessage(stringResource(R.string.jadwal_sholat_city_no_results, uiState.cityQuery))

        else ->
            LazyColumn(
                // Keeps the last row off the keyboard/gesture bar when the sheet sizes to content.
                contentPadding = PaddingValues(bottom = SanguSantriSpacing.default),
                modifier = Modifier.fillMaxWidth(),
            ) {
                pinned?.let { city ->
                    item(key = "pinned-${city.id}") {
                        CityRow(name = city.name, selected = true, onClick = { actions.onCitySelected(city.id) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
                items(items = results, key = { it.id }) { city ->
                    CityRow(
                        name = city.name,
                        selected = city.id == selected?.id,
                        onClick = { actions.onCitySelected(city.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
    }
}

@Composable
private fun CityPickerMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = SectionGap),
    )
}

@Composable
private fun CityRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .heightIn(min = CityRowMinHeight)
                .padding(horizontal = ScreenPadding, vertical = SanguSantriSpacing.medium),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = stringResource(R.string.jadwal_sholat_city_selected),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** "Senin, 17 Agustus 2026 · 5 Rabiulawal 1448" — gregorian from the device locale, hijri from the
 * app's own offline computation, with the month names the rest of the app already uses. */
private fun LocalDate.formatWithHijri(hijriMonthNames: List<String>): String {
    val hijri = HijrahDate.from(this)
    val hijriMonth = hijri.get(ChronoField.MONTH_OF_YEAR)
    val monthName = hijriMonthNames.getOrNull(hijriMonth - 1).orEmpty()
    val gregorian = format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")))
    return "$gregorian · ${hijri.get(ChronoField.DAY_OF_MONTH)} $monthName ${hijri.get(ChronoField.YEAR)}"
}

private val SearchFieldCornerRadius = 16.dp
private val CityRowMinHeight = 56.dp
private val NeedleInset = 10.dp
private val NeedleStroke = 2.dp
private val NeedleHubRadius = 4.dp
private val ScreenPadding = 20.dp
private val SectionGap = 22.dp
private val BlockCornerRadius = 24.dp
private val BlockPadding = 18.dp
private val CardPadding = 18.dp
private val CompassSize = 104.dp
private val CurrentRowBleed = 14.dp
private val CurrentRowCornerRadius = 16.dp
private val CountdownSize = 44.sp
private val BearingSize = 32.sp
