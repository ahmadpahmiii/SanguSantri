package com.sangusantri.app.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import com.sangusantri.app.domain.usecase.CancelReminderUseCase
import com.sangusantri.app.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Owns the Pengingat Amaliyah (`0.0.4`) list screen state — combines every reminder with the
 * bundled amaliyah catalogue so rows/the create form can show a real title, never just a raw id. */
@HiltViewModel
class ReminderViewModel
@Inject
constructor(
    private val reminderRepository: ReminderRepository,
    contentRepository: ContentRepository,
    private val scheduleReminder: ScheduleReminderUseCase,
    private val cancelReminder: CancelReminderUseCase,
) : ViewModel() {
    val uiState: StateFlow<ReminderUiState> =
        combine(
            reminderRepository.observeAll(),
            contentRepository.observeActiveContent(),
        ) { reminders, content ->
            ReminderUiState.Loaded(reminders = reminders, availableContent = content)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ReminderUiState.Loading,
        )

    fun onAction(action: ReminderUiAction) {
        when (action) {
            is ReminderUiAction.SaveReminder -> viewModelScope.launch { scheduleReminder(action.reminder) }

            is ReminderUiAction.ToggleEnabled ->
                viewModelScope.launch {
                    scheduleReminder(
                        action.reminder.copy(isEnabled = !action.reminder.isEnabled),
                    )
                }

            is ReminderUiAction.DeleteReminder -> viewModelScope.launch { cancelReminder(action.reminderId) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
