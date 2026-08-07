package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One personal reminder (`0.0.4`, Pengingat Amaliyah). Flat columns mirror
 * [com.sangusantri.app.domain.model.ReminderSchedule]'s two sealed cases — `dayOfWeek` is set only
 * for `WEEKLY`, `hijriMonth`/`hijriDay`/`repeatsYearly` only for `HIJRI_DATE`.
 * `nextTriggerAtEpochMillis` is persisted (not recomputed on every read) so
 * `ReminderDao.observeNearestEnabled` can order/limit in SQL without loading every row into Kotlin.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["contentId"]), Index(value = ["isEnabled", "nextTriggerAtEpochMillis"])],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val contentId: String,
    val label: String,
    val scheduleKind: String,
    val dayOfWeek: Int?,
    val hijriMonth: Int?,
    val hijriDay: Int?,
    val repeatsYearly: Boolean?,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val nextTriggerAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
)
