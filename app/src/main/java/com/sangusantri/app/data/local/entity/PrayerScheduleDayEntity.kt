package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * One day's published prayer times for one city.
 *
 * myquran returns a whole month in a single `/sholat/jadwal/{id}/{yyyy-MM}` call, so a month is
 * fetched at once and every day of it lands here — the schedule then keeps working offline for the
 * rest of the month, which is the behaviour an offline-first app needs (`OFFLINE_FIRST.md`).
 *
 * Times are stored exactly as published (`HH:mm` strings), not parsed into a local type, so nothing
 * this app does can shift a published prayer time. Parsing happens at the domain boundary.
 */
@Entity(
    tableName = "prayer_schedule_days",
    primaryKeys = ["cityId", "date"],
    indices = [Index(value = ["cityId", "date"])],
)
data class PrayerScheduleDayEntity(
    val cityId: String,
    /** ISO `yyyy-MM-dd`, the key myquran itself uses. */
    val date: String,
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String,
)
