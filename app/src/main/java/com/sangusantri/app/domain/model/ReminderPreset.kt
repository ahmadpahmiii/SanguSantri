package com.sangusantri.app.domain.model

import java.time.DayOfWeek

/**
 * Reminder-creation-form presets (`0.0.4`, ROADMAP.md: "Tahlil malam Jumat and Istighosah weekly
 * presets"). Pre-fill only — the form always shows the resulting day/time and lets the user adjust
 * it before saving, so nothing is silently scheduled without confirmation.
 */
enum class ReminderPreset(
    val contentId: String?,
    val defaultDayOfWeek: DayOfWeek?,
    val defaultHour: Int,
    val defaultMinute: Int,
) {
    /** "Malam Jumat" — Thursday evening, the traditional NU Tahlil timing this preset names. */
    TAHLIL_THURSDAY_NIGHT(
        contentId = "tahlil",
        defaultDayOfWeek = DayOfWeek.THURSDAY,
        defaultHour = 19,
        defaultMinute = 0,
    ),
    ISTIGHOSAH_WEEKLY(
        contentId = "istighosah",
        defaultDayOfWeek = DayOfWeek.FRIDAY,
        defaultHour = 5,
        defaultMinute = 0,
    ),
    CUSTOM(
        contentId = null,
        defaultDayOfWeek = null,
        defaultHour = 19,
        defaultMinute = 0,
    ),
}
