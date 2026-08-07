package com.sangusantri.app.domain.usecase

import com.sangusantri.app.data.reminder.ReminderAlarmScheduler
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.ReminderScheduleCalculator
import com.sangusantri.app.domain.repository.ReminderRepository
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Persists a [Reminder] and arms its [ReminderAlarmScheduler] alarm in one step — used by the
 * create/edit form and by [RescheduleAllRemindersUseCase]. Combining a repository write with an
 * `AlarmManager` side effect is exactly the kind of cross-cutting logic `CODING_STANDARD.md` says
 * justifies a use case over a plain repository call.
 */
class ScheduleReminderUseCase
@Inject
constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: ReminderAlarmScheduler,
) {
    suspend operator fun invoke(reminder: Reminder) {
        val withNextTrigger =
            reminder.copy(
                nextTriggerAtEpochMillis =
                    ReminderScheduleCalculator
                        .nextTrigger(
                            reminder.schedule,
                            ZonedDateTime.now(),
                        ).toInstant()
                        .toEpochMilli(),
            )
        reminderRepository.save(withNextTrigger)
        if (withNextTrigger.isEnabled) {
            alarmScheduler.scheduleAlarm(withNextTrigger)
        } else {
            alarmScheduler.cancelAlarm(withNextTrigger.id)
        }
    }
}
