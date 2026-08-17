package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One kabupaten/kota from myquran's `/sholat/kabkota/semua` (~517 rows, ~35 KB).
 *
 * Prayer times are keyed by this id, not by coordinates, which is why the schedule needs no
 * location permission at all — the user picks their city once. Fetched on first use and cached
 * here so the picker works offline afterwards.
 */
@Entity(tableName = "prayer_cities")
data class PrayerCityEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Lowercased [name], stored so the picker's search is a plain indexed LIKE rather than a
     * case-folding pass over every row on each keystroke. */
    val searchName: String,
)
