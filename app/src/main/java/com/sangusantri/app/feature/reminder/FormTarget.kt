package com.sangusantri.app.feature.reminder

import com.sangusantri.app.domain.model.Reminder

/** Which overlay [ReminderScreen] should show — a brand-new reminder or editing an existing one. */
internal sealed interface FormTarget {
    data object New : FormTarget

    data class Edit(
        val reminder: Reminder,
    ) : FormTarget
}
