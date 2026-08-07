package com.sangusantri.app.data.repository

import com.sangusantri.app.data.local.dao.ReminderDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl
@Inject
constructor(
    private val reminderDao: ReminderDao,
) : ReminderRepository {
    override suspend fun save(reminder: Reminder) = reminderDao.upsert(reminder.toEntity())

    override suspend fun delete(reminderId: String) = reminderDao.deleteById(reminderId)

    override suspend fun getById(reminderId: String): Reminder? = reminderDao.getById(reminderId)?.toDomain()

    override suspend fun getAllEnabled(): List<Reminder> = reminderDao.getAllEnabled().map { it.toDomain() }

    override fun observeAll(): Flow<List<Reminder>> =
        reminderDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeNearestEnabled(): Flow<Reminder?> =
        reminderDao.observeNearestEnabled().map { it?.toDomain() }
}
