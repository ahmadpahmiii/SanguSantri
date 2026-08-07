package com.sangusantri.app.domain.usecase

import com.sangusantri.app.domain.repository.ReminderRepository
import javax.inject.Inject

/**
 * Recomputes and re-arms every enabled reminder's alarm — `AlarmManager` alarms do not survive a
 * device reboot, so `ReminderBootReceiver` calls this on `BOOT_COMPLETED` (ROADMAP.md's
 * "rescheduling after reboot" requirement). Reuses [ScheduleReminderUseCase] per reminder rather
 * than duplicating the persist-then-arm logic.
 */
class RescheduleAllRemindersUseCase
@Inject
constructor(
    private val reminderRepository: ReminderRepository,
    private val scheduleReminder: ScheduleReminderUseCase,
) {
    suspend operator fun invoke() {
        reminderRepository.getAllEnabled().forEach { reminder -> scheduleReminder(reminder) }
    }
}
