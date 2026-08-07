package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.ReminderEntity
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.ReminderSchedule
import java.time.DayOfWeek

private const val SCHEDULE_KIND_WEEKLY = "WEEKLY"
private const val SCHEDULE_KIND_HIJRI_DATE = "HIJRI_DATE"

fun ReminderEntity.toDomain(): Reminder =
    Reminder(
        id = id,
        contentId = contentId,
        label = label,
        schedule = scheduleToDomain(),
        isEnabled = isEnabled,
        nextTriggerAtEpochMillis = nextTriggerAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
    )

private fun ReminderEntity.scheduleToDomain(): ReminderSchedule =
    when (scheduleKind) {
        SCHEDULE_KIND_WEEKLY ->
            ReminderSchedule.Weekly(
                dayOfWeek = DayOfWeek.of(requireNotNull(dayOfWeek) { "WEEKLY reminder $id missing dayOfWeek" }),
                hour = hour,
                minute = minute,
            )

        SCHEDULE_KIND_HIJRI_DATE ->
            ReminderSchedule.HijriDate(
                hijriMonth = requireNotNull(hijriMonth) { "HIJRI_DATE reminder $id missing hijriMonth" },
                hijriDay = requireNotNull(hijriDay) { "HIJRI_DATE reminder $id missing hijriDay" },
                hour = hour,
                minute = minute,
                repeatsYearly = requireNotNull(repeatsYearly) { "HIJRI_DATE reminder $id missing repeatsYearly" },
            )

        else -> error("Unknown reminder scheduleKind '$scheduleKind' for reminder $id")
    }

fun Reminder.toEntity(): ReminderEntity =
    ReminderEntity(
        id = id,
        contentId = contentId,
        label = label,
        scheduleKind =
            when (schedule) {
                is ReminderSchedule.Weekly -> SCHEDULE_KIND_WEEKLY
                is ReminderSchedule.HijriDate -> SCHEDULE_KIND_HIJRI_DATE
            },
        dayOfWeek = (schedule as? ReminderSchedule.Weekly)?.dayOfWeek?.value,
        hijriMonth = (schedule as? ReminderSchedule.HijriDate)?.hijriMonth,
        hijriDay = (schedule as? ReminderSchedule.HijriDate)?.hijriDay,
        repeatsYearly = (schedule as? ReminderSchedule.HijriDate)?.repeatsYearly,
        hour = schedule.hour,
        minute = schedule.minute,
        isEnabled = isEnabled,
        nextTriggerAtEpochMillis = nextTriggerAtEpochMillis,
        createdAtEpochMillis = createdAtEpochMillis,
    )
