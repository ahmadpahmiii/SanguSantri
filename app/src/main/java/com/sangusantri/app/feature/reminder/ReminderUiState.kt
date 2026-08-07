package com.sangusantri.app.feature.reminder

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.Reminder

/** Pengingat Amaliyah (`0.0.4`) list screen state. */
sealed interface ReminderUiState {
    data object Loading : ReminderUiState

    data class Loaded(
        val reminders: List<Reminder>,
        /** The bundled amaliyah catalogue — the create/edit form's "which amaliyah" picker only
         * ever offers real, existing content, never an invented one. */
        val availableContent: List<Content>,
    ) : ReminderUiState {
        fun contentTitleFor(contentId: String): String? = availableContent.find { it.id == contentId }?.title
    }
}
