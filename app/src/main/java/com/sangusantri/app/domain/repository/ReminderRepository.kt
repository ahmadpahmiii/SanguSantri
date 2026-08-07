package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

/** Reads and writes personal reminders (`0.0.4`, Pengingat Amaliyah) via Room. */
interface ReminderRepository {
    suspend fun save(reminder: Reminder)

    suspend fun delete(reminderId: String)

    suspend fun getById(reminderId: String): Reminder?

    suspend fun getAllEnabled(): List<Reminder>

    fun observeAll(): Flow<List<Reminder>>

    /** The single soonest-firing enabled reminder, or `null` — backs Beranda's "nearest reminder" section. */
    fun observeNearestEnabled(): Flow<Reminder?>
}
