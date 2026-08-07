package com.sangusantri.app.domain.model

/** A personal amaliyah reminder (`0.0.4`, Pengingat Amaliyah) — always tied to one [Content] item. */
data class Reminder(
    val id: String,
    val contentId: String,
    val label: String,
    val schedule: ReminderSchedule,
    val isEnabled: Boolean,
    val nextTriggerAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
)
