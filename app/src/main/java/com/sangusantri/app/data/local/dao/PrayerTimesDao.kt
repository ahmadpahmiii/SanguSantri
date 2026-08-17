package com.sangusantri.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sangusantri.app.data.local.entity.PrayerCityEntity
import com.sangusantri.app.data.local.entity.PrayerScheduleDayEntity
import kotlinx.coroutines.flow.Flow

/** One DAO for both prayer-times tables — they are read and written together and never used apart,
 * so splitting them would only add a file (`CODING_STANDARD.md`'s no-duplicate-DAO rule). */
@Dao
interface PrayerTimesDao {
    @Query("SELECT COUNT(*) FROM prayer_cities")
    suspend fun cityCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCities(cities: List<PrayerCityEntity>)

    @Query("SELECT * FROM prayer_cities ORDER BY name LIMIT :limit")
    fun observeCities(limit: Int): Flow<List<PrayerCityEntity>>

    @Query(
        "SELECT * FROM prayer_cities WHERE searchName LIKE '%' || :query || '%' ORDER BY name LIMIT :limit",
    )
    fun searchCities(
        query: String,
        limit: Int,
    ): Flow<List<PrayerCityEntity>>

    /** All 517 rows. Small enough to match against in Kotlin, which the geocoder-name matching
     * needs — normalising "KOTA ADM. JAKARTA BARAT" is not something to express in SQL. */
    @Query("SELECT * FROM prayer_cities")
    suspend fun allCities(): List<PrayerCityEntity>

    @Query("SELECT * FROM prayer_cities WHERE id = :cityId")
    fun observeCity(cityId: String): Flow<PrayerCityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheduleDays(days: List<PrayerScheduleDayEntity>)

    @Query("SELECT * FROM prayer_schedule_days WHERE cityId = :cityId AND date = :date")
    fun observeDay(
        cityId: String,
        date: String,
    ): Flow<PrayerScheduleDayEntity?>

    /** How much of the requested month is already cached — the refresh trigger. */
    @Query("SELECT COUNT(*) FROM prayer_schedule_days WHERE cityId = :cityId AND date LIKE :monthPrefix || '%'")
    suspend fun countDaysInMonth(
        cityId: String,
        monthPrefix: String,
    ): Int

    /** Keeps the table from growing without bound: only the months around today are worth keeping. */
    @Query("DELETE FROM prayer_schedule_days WHERE date < :beforeDate")
    suspend fun deleteDaysBefore(beforeDate: String)
}
