package com.sangusantri.app.domain.usecase

import com.sangusantri.app.data.reminder.ReminderAlarmScheduler
import com.sangusantri.app.domain.repository.ReminderRepository
import javax.inject.Inject

/** Deletes a reminder from Room and unschedules its alarm together — never one without the other. */
class CancelReminderUseCase
@Inject
constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: ReminderAlarmScheduler,
) {
    suspend operator fun invoke(reminderId: String) {
        alarmScheduler.cancelAlarm(reminderId)
        reminderRepository.delete(reminderId)
    }
}
