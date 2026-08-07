package com.sangusantri.app.feature.reminder.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.ReminderPreset
import com.sangusantri.app.domain.model.ReminderSchedule
import com.sangusantri.app.domain.model.ReminderScheduleCalculator
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.UUID

private val SheetTopCornerRadius = 28.dp

private enum class ScheduleMode { WEEKLY, HIJRI }

/**
 * The Pengingat creation/edit form — a bottom sheet, never a full-screen form (`DESIGN_SYSTEM.md`
 * decision J's established precedent, `CustomTasbihTargetDialog.kt`). [existing] non-null opens
 * this in edit mode, pre-filled; null opens it for a brand-new reminder. Presets
 * ([ReminderPreset]) only pre-fill fields the user still sees and can change before
 * [onSave] — nothing is scheduled without the user seeing the exact resulting day/time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormSheet(
    availableContent: List<Content>,
    existing: Reminder?,
    onSave: (Reminder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = SheetTopCornerRadius, topEnd = SheetTopCornerRadius),
    ) {
        ReminderFormContent(
            availableContent = availableContent,
            existing = existing,
            onSave = onSave,
            onDismiss = onDismiss,
        )
    }
}

/** Every pre-fill value [ReminderFormContent] needs, computed once outside the composable itself
 * so its own branching doesn't count toward that function's cyclomatic complexity. */
private data class ReminderFormInitialState(
    val preset: ReminderPreset,
    val contentId: String?,
    val label: String,
    val mode: ScheduleMode,
    val dayOfWeek: DayOfWeek,
    val hijriMonth: Int,
    val hijriDay: Int,
    val repeatsYearly: Boolean,
    val hour: Int,
    val minute: Int,
)

private fun initialFormState(
    existing: Reminder?,
    availableContent: List<Content>,
    currentHijri: HijrahDate,
): ReminderFormInitialState {
    val schedule = existing?.schedule
    val hijri = schedule as? ReminderSchedule.HijriDate
    val weekly = schedule as? ReminderSchedule.Weekly
    return ReminderFormInitialState(
        preset = presetFor(existing),
        contentId = existing?.contentId ?: availableContent.firstOrNull()?.id,
        label = existing?.label.orEmpty(),
        mode = if (hijri != null) ScheduleMode.HIJRI else ScheduleMode.WEEKLY,
        dayOfWeek = weekly?.dayOfWeek ?: DayOfWeek.THURSDAY,
        hijriMonth = hijri?.hijriMonth ?: currentHijri.get(ChronoField.MONTH_OF_YEAR),
        hijriDay = hijri?.hijriDay ?: currentHijri.get(ChronoField.DAY_OF_MONTH),
        repeatsYearly = hijri?.repeatsYearly ?: true,
        hour = schedule?.hour ?: ReminderPreset.TAHLIL_THURSDAY_NIGHT.defaultHour,
        minute = schedule?.minute ?: ReminderPreset.TAHLIL_THURSDAY_NIGHT.defaultMinute,
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderFormContent(
    availableContent: List<Content>,
    existing: Reminder?,
    onSave: (Reminder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentHijri = remember { ReminderScheduleCalculator.currentHijriDate() }
    val initial = remember { initialFormState(existing, availableContent, currentHijri) }
    var selectedPreset by rememberSaveable { mutableStateOf(initial.preset) }
    var contentId by rememberSaveable { mutableStateOf(initial.contentId) }
    var label by rememberSaveable { mutableStateOf(initial.label) }
    var mode by rememberSaveable { mutableStateOf(initial.mode) }
    var dayOfWeek by rememberSaveable { mutableStateOf(initial.dayOfWeek) }
    var hijriMonth by rememberSaveable { mutableIntStateOf(initial.hijriMonth) }
    var hijriDay by rememberSaveable { mutableIntStateOf(initial.hijriDay) }
    var repeatsYearly by rememberSaveable { mutableStateOf(initial.repeatsYearly) }
    val timePickerState =
        rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(SanguSantriSpacing.default)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        Text(
            text =
                stringResource(
                    if (existing == null) R.string.reminder_form_title_add else R.string.reminder_form_title_edit,
                ),
            style = MaterialTheme.typography.titleLarge,
        )

        val tahlilPresetLabel = stringResource(R.string.reminder_preset_tahlil_thursday_night)
        val istighosahPresetLabel = stringResource(R.string.reminder_preset_istighosah_weekly)
        ReminderPresetChips(
            selected = selectedPreset,
            onSelected = { preset ->
                selectedPreset = preset
                preset.contentId?.let { contentId = it }
                preset.defaultDayOfWeek?.let { dayOfWeek = it }
                if (preset != ReminderPreset.CUSTOM) mode = ScheduleMode.WEEKLY
                // Only pre-fill when still blank — never overwrite a label the user already typed.
                if (label.isBlank()) {
                    label =
                        when (preset) {
                            ReminderPreset.TAHLIL_THURSDAY_NIGHT -> tahlilPresetLabel
                            ReminderPreset.ISTIGHOSAH_WEEKLY -> istighosahPresetLabel
                            ReminderPreset.CUSTOM -> label
                        }
                }
            },
        )

        ContentPicker(
            availableContent = availableContent,
            selectedContentId = contentId,
            onSelected = { contentId = it },
        )

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.reminder_form_label_field)) },
            singleLine = true,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = mode == ScheduleMode.WEEKLY,
                onClick = { mode = ScheduleMode.WEEKLY },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(text = stringResource(R.string.reminder_form_mode_weekly)) }
            SegmentedButton(
                selected = mode == ScheduleMode.HIJRI,
                onClick = { mode = ScheduleMode.HIJRI },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(text = stringResource(R.string.reminder_form_mode_hijri)) }
        }

        if (mode == ScheduleMode.WEEKLY) {
            DayOfWeekPicker(selected = dayOfWeek, onSelected = { dayOfWeek = it })
        } else {
            HijriDatePicker(
                month = hijriMonth,
                day = hijriDay,
                repeatsYearly = repeatsYearly,
                onMonthChange = { hijriMonth = it },
                onDayChange = { hijriDay = it },
                onRepeatsYearlyChange = { repeatsYearly = it },
            )
        }

        TimeInput(state = timePickerState)

        Button(
            onClick = {
                val schedule =
                    if (mode == ScheduleMode.WEEKLY) {
                        ReminderSchedule.Weekly(dayOfWeek, timePickerState.hour, timePickerState.minute)
                    } else {
                        ReminderSchedule.HijriDate(
                            hijriMonth,
                            hijriDay,
                            timePickerState.hour,
                            timePickerState.minute,
                            repeatsYearly,
                        )
                    }
                val resolvedContentId = contentId ?: return@Button
                onSave(buildReminder(existing, resolvedContentId, label, schedule))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = contentId != null,
        ) {
            Text(text = stringResource(R.string.reminder_form_save_action))
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.reminder_form_cancel_action))
        }
    }
}

private fun buildReminder(
    existing: Reminder?,
    contentId: String,
    label: String,
    schedule: ReminderSchedule,
): Reminder =
    Reminder(
        id = existing?.id ?: UUID.randomUUID().toString(),
        contentId = contentId,
        label = label,
        schedule = schedule,
        isEnabled = existing?.isEnabled ?: true,
        nextTriggerAtEpochMillis =
            ReminderScheduleCalculator.nextTrigger(schedule, ZonedDateTime.now()).toInstant().toEpochMilli(),
        createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis(),
    )

private fun presetFor(existing: Reminder?): ReminderPreset =
    when {
        existing == null -> ReminderPreset.TAHLIL_THURSDAY_NIGHT
        else -> ReminderPreset.CUSTOM
    }

@Composable
private fun ReminderPresetChips(
    selected: ReminderPreset,
    onSelected: (ReminderPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        FilterChip(
            selected = selected == ReminderPreset.TAHLIL_THURSDAY_NIGHT,
            onClick = { onSelected(ReminderPreset.TAHLIL_THURSDAY_NIGHT) },
            label = { Text(text = stringResource(R.string.reminder_preset_tahlil_thursday_night)) },
            shape = SanguSantriShapes.extraLarge,
        )
        FilterChip(
            selected = selected == ReminderPreset.ISTIGHOSAH_WEEKLY,
            onClick = { onSelected(ReminderPreset.ISTIGHOSAH_WEEKLY) },
            label = { Text(text = stringResource(R.string.reminder_preset_istighosah_weekly)) },
            shape = SanguSantriShapes.extraLarge,
        )
        FilterChip(
            selected = selected == ReminderPreset.CUSTOM,
            onClick = { onSelected(ReminderPreset.CUSTOM) },
            label = { Text(text = stringResource(R.string.reminder_preset_custom)) },
            shape = SanguSantriShapes.extraLarge,
        )
    }
}

@Composable
private fun ContentPicker(
    availableContent: List<Content>,
    selectedContentId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        availableContent.forEach { content ->
            FilterChip(
                selected = selectedContentId == content.id,
                onClick = { onSelected(content.id) },
                label = { Text(text = content.title) },
                shape = SanguSantriShapes.extraLarge,
            )
        }
    }
}

@Composable
private fun DayOfWeekPicker(
    selected: DayOfWeek,
    onSelected: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayNames = stringArrayResource(R.array.reminder_day_names)
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        DayOfWeek.entries.forEach { day ->
            FilterChip(
                selected = selected == day,
                onClick = { onSelected(day) },
                label = { Text(text = dayNames.getOrElse(day.value - 1) { day.name }) },
                shape = SanguSantriShapes.extraLarge,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun HijriDatePicker(
    month: Int,
    day: Int,
    repeatsYearly: Boolean,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onRepeatsYearlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(R.array.reminder_hijri_month_names)
    val currentHijriYear = remember { ReminderScheduleCalculator.currentHijriDate().get(ChronoField.YEAR) }
    val monthLength = remember(month) { ReminderScheduleCalculator.hijriMonthLength(currentHijriYear, month) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
            monthNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = month == index + 1,
                    onClick = { onMonthChange(index + 1) },
                    label = { Text(text = name) },
                    shape = SanguSantriShapes.extraLarge,
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
            for (candidateDay in 1..monthLength) {
                FilterChip(
                    selected = day == candidateDay,
                    onClick = { onDayChange(candidateDay) },
                    label = { Text(text = candidateDay.toString()) },
                    shape = SanguSantriShapes.extraLarge,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.reminder_form_repeats_yearly), modifier = Modifier.weight(1f))
            Switch(checked = repeatsYearly, onCheckedChange = onRepeatsYearlyChange)
        }
    }
}

@PreviewLightDark
@Composable
private fun ReminderFormSheetPreview() {
    SanguSantriTheme {
        ReminderFormContent(
            availableContent =
                listOf(
                    Content(
                        id = "tahlil",
                        title = "Tahlil",
                        description = "",
                        imageUrl = null,
                        category = null,
                        version = 1,
                        order = 1,
                        isActive = true,
                        sourceName = "",
                        sourceUrl = "",
                    ),
                ),
            existing = null,
            onSave = {},
            onDismiss = {},
        )
    }
}
