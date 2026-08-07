package com.sangusantri.app.feature.reminder

import com.sangusantri.app.domain.model.Reminder

/** User-initiated intents the Pengingat screen sends to [ReminderViewModel] (unidirectional data flow). */
sealed interface ReminderUiAction {
    /** Dispatched only after the create/edit form's own validation already accepted [reminder] —
     * the ViewModel never re-validates, matching
     * [com.sangusantri.app.feature.tasbih.TasbihUiAction.SetCustomTarget]'s pattern. */
    data class SaveReminder(
        val reminder: Reminder,
    ) : ReminderUiAction

    data class ToggleEnabled(
        val reminder: Reminder,
    ) : ReminderUiAction

    /** Confirmed via a dialog in the UI layer — the ViewModel never deletes without confirmation. */
    data class DeleteReminder(
        val reminderId: String,
    ) : ReminderUiAction
}
