package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.Reminder

/** Beranda screen state. [Loaded] with an empty list is a valid state (nothing synced yet). */
sealed interface SerambiUiState {
    data object Loading : SerambiUiState

    data class Loaded(
        val items: List<Content>,
        /** `0.0.4`, Pengingat Amaliyah — the single soonest-firing enabled reminder, or `null`
         * (hides the "Pengingat terdekat" section; PRD §8.6 decision list item 4). */
        val nearestReminder: Reminder? = null,
        /** `0.0.5`, Nahwu Quiz — whether any bundled quiz package exists, hiding the "Belajar"
         * section otherwise (PRD §8.6 decision list item 4's hide-until-real-data rule). */
        val hasNahwuQuizContent: Boolean = false,
    ) : SerambiUiState {
        fun nearestReminderContentTitle(): String? = items.find { it.id == nearestReminder?.contentId }?.title
    }
}
