package com.sangusantri.app.domain.model

import java.time.DayOfWeek

/**
 * How a [Reminder] recurs (`0.0.4`, Pengingat Amaliyah). Every reminder is either day-of-week
 * based (the two named presets — Tahlil malam Jumat, Istighosah weekly — are inherently weekly,
 * not anchored to a specific Islamic date) or anchored to a specific Hijri calendar date.
 */
sealed interface ReminderSchedule {
    val hour: Int
    val minute: Int

    data class Weekly(
        val dayOfWeek: DayOfWeek,
        override val hour: Int,
        override val minute: Int,
    ) : ReminderSchedule

    /** [repeatsYearly] = false fires exactly once, then the reminder disables itself rather than
     * being deleted — preserves history the same way completed Tasbih/Guided Reader state does. */
    data class HijriDate(
        val hijriMonth: Int,
        val hijriDay: Int,
        override val hour: Int,
        override val minute: Int,
        val repeatsYearly: Boolean,
    ) : ReminderSchedule
}
