package com.sangusantri.app.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sangusantri.app.data.local.dao.PrayerTimesDao
import com.sangusantri.app.data.local.entity.PrayerCityEntity
import com.sangusantri.app.data.local.entity.PrayerScheduleDayEntity
import com.sangusantri.app.data.location.DeviceLocationSource
import com.sangusantri.app.data.remote.prayertimes.api.PrayerTimesApiService
import com.sangusantri.app.data.remote.prayertimes.dto.PrayerScheduleEntryDto
import com.sangusantri.app.domain.model.CityDetection
import com.sangusantri.app.domain.model.PrayerCity
import com.sangusantri.app.domain.model.PrayerName
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.PrayerTime
import com.sangusantri.app.domain.repository.PrayerScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Jadwal Sholat backed by myquran, with Room as the source of truth — the UI reads only what has
 * been persisted, never a network response (`ARCHITECTURE.md`'s boundary rule).
 *
 * Times are stored and returned exactly as published. The only transformation is parsing `"HH:mm"`
 * into [LocalTime] at this boundary; a value that will not parse is dropped rather than guessed at,
 * because a wrong prayer time is worse than a missing one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class PrayerScheduleRepositoryImpl
@Inject
constructor(
    private val dao: PrayerTimesDao,
    private val apiService: PrayerTimesApiService,
    private val locationSource: DeviceLocationSource,
    private val dataStore: DataStore<Preferences>,
) : PrayerScheduleRepository {
    private val preferences: Flow<Preferences> =
        dataStore.data.catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    override fun observeToday(): Flow<PrayerSchedule?> =
        preferences
            .map { it[SELECTED_CITY_ID] }
            .flatMapLatest { cityId ->
                if (cityId == null) {
                    flowOf(null)
                } else {
                    combine(
                        dao.observeDay(cityId, LocalDate.now().format(ISO_DATE)),
                        dao.observeCity(cityId),
                        preferences,
                    ) { day, city, prefs ->
                        day?.toSchedule(city?.name ?: "", prefs)
                    }
                }
            }

    override fun observeSelectedCity(): Flow<PrayerCity?> =
        preferences
            .map { it[SELECTED_CITY_ID] }
            .flatMapLatest { cityId ->
                if (cityId == null) flowOf(null) else dao.observeCity(cityId).map { it?.toDomain() }
            }

    override fun observeCities(query: String): Flow<List<PrayerCity>> =
        if (query.isBlank()) {
            dao.observeCities(CITY_PAGE_LIMIT).map { rows -> rows.map { it.toDomain() } }
        } else {
            dao.searchCities(query.lowercase(), CITY_PAGE_LIMIT).map { rows -> rows.map { it.toDomain() } }
        }

    override suspend fun ensureCitiesCached(): Result<Unit> {
        if (dao.cityCount() > 0) return Result.success(Unit)
        return runCatching {
            val response = apiService.getCities()
            val cities =
                response.body()?.takeIf { response.isSuccessful && it.status }?.data
                    ?: error("city list unavailable")
            dao.upsertCities(
                cities.map { PrayerCityEntity(id = it.id, name = it.lokasi, searchName = it.lokasi.lowercase()) },
            )
        }
    }

    override suspend fun selectCity(cityId: String) {
        dataStore.edit { it[SELECTED_CITY_ID] = cityId }
    }

    override suspend fun detectAndSelectCity(): CityDetection =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureCitiesCached().getOrThrow()
                // currentLocation(), not lastKnownLocation(): right after the permission grant there
                // is often nothing cached, and that is exactly when this runs.
                val location = locationSource.currentLocation() ?: return@runCatching CityDetection.Unavailable
                val candidates = locationSource.kabkotaCandidates(location)
                if (candidates.isEmpty()) return@runCatching CityDetection.Unavailable
                resolveCity(candidates)
            }.onFailure { failure ->
                Log.w(TAG, "city detection failed: ${failure.message}")
            }.getOrDefault(CityDetection.Unavailable)
        }

    /**
     * Turns the geocoder's place names into a city, or admits it cannot.
     *
     * The two sources disagree on granularity and wording: the geocoder says "Kota Jakarta Barat"
     * where myquran has a single "KOTA JAKARTA", and plain "Bandung" is both a kota and a kabupaten.
     * So a unique match is required — where several cities are plausible the reader confirms in the
     * picker rather than the app quietly choosing one and showing another city's prayer times.
     */
    private suspend fun resolveCity(candidates: List<String>): CityDetection {
        val cities = dao.allCities()
        val resolved =
            candidates.firstNotNullOfOrNull { candidate ->
                val matches = cities.matching(candidate)
                when {
                    matches.isEmpty() -> null
                    matches.size == 1 -> CityDetection.Detected(matches.first().toDomain())
                    else -> {
                        // Counts only, never the place names — those are the reader's location.
                        Log.w(TAG, "city detection ambiguous (${matches.size} candidate cities)")
                        CityDetection.Ambiguous(candidate.pickerQuery())
                    }
                }
            }
        if (resolved is CityDetection.Detected) selectCity(resolved.city.id)
        if (resolved == null) Log.w(TAG, "city detection: no city matched (${candidates.size} names tried)")
        return resolved ?: CityDetection.Ambiguous(candidates.first().pickerQuery())
    }

    override fun observeLocationPromptShown(): Flow<Boolean> = preferences.map { it[LOCATION_PROMPT_SHOWN] ?: false }

    override suspend fun markLocationPromptShown() {
        dataStore.edit { it[LOCATION_PROMPT_SHOWN] = true }
    }

    override suspend fun ensureScheduleCached(month: LocalDate): Result<Unit> {
        val cityId = preferences.first()[SELECTED_CITY_ID]
        val monthPrefix = month.format(ISO_MONTH)
        // Nothing to do without a city, or when the month is already complete in Room — the second
        // case is what lets a returning reader open the screen offline without an error.
        val alreadyCached =
            cityId != null && dao.countDaysInMonth(cityId, monthPrefix) >= month.lengthOfMonth()
        if (cityId == null || alreadyCached) return Result.success(Unit)
        return runCatching {
            val response = apiService.getSchedule(cityId, monthPrefix)
            val schedule =
                response.body()?.takeIf { response.isSuccessful && it.status }?.data
                    ?: error("schedule unavailable")
            dao.upsertScheduleDays(
                schedule.jadwal.map { (date, entry) -> entry.toEntity(cityId, date) },
            )
            // A month either side of today is all this screen can show; the rest is dead weight.
            dao.deleteDaysBefore(month.minusMonths(1).withDayOfMonth(1).format(ISO_DATE))
        }
    }

    override suspend fun setNotificationEnabled(
        prayer: PrayerName,
        enabled: Boolean,
    ) {
        dataStore.edit { it[notificationKey(prayer)] = enabled }
    }

    private fun PrayerScheduleDayEntity.toSchedule(
        cityName: String,
        prefs: Preferences,
    ): PrayerSchedule? {
        val times =
            listOfNotNull(
                prayerTime(PrayerName.IMSAK, imsak, prefs),
                prayerTime(PrayerName.SUBUH, subuh, prefs),
                prayerTime(PrayerName.ZUHUR, dzuhur, prefs),
                prayerTime(PrayerName.ASAR, ashar, prefs),
                prayerTime(PrayerName.MAGRIB, maghrib, prefs),
                prayerTime(PrayerName.ISYA, isya, prefs),
            )
        // An incomplete day is not a schedule anyone should pray by.
        if (times.size < PrayerName.entries.size) return null
        return PrayerSchedule(times = times, location = cityName, source = SOURCE_NAME)
    }

    private fun prayerTime(
        name: PrayerName,
        published: String,
        prefs: Preferences,
    ): PrayerTime? {
        val time = runCatching { LocalTime.parse(published, PUBLISHED_TIME) }.getOrNull() ?: return null
        val default = name != PrayerName.IMSAK
        return PrayerTime(
            name = name,
            time = time,
            notificationEnabled = prefs[notificationKey(name)] ?: default,
        )
    }

    private fun PrayerScheduleEntryDto.toEntity(
        cityId: String,
        date: String,
    ) = PrayerScheduleDayEntity(
        cityId = cityId,
        date = date,
        imsak = imsak,
        subuh = subuh,
        terbit = terbit,
        dhuha = dhuha,
        dzuhur = dzuhur,
        ashar = ashar,
        maghrib = maghrib,
        isya = isya,
    )

    private fun PrayerCityEntity.toDomain() = PrayerCity(id = id, name = name)

    private fun notificationKey(prayer: PrayerName) = booleanPreferencesKey("prayer_notification_${prayer.name}")

    private companion object {
        const val TAG = "PrayerSchedule"
        val SELECTED_CITY_ID = stringPreferencesKey("prayer_selected_city_id")
        val LOCATION_PROMPT_SHOWN = booleanPreferencesKey("prayer_location_prompt_shown")
        val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val ISO_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val PUBLISHED_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        const val CITY_PAGE_LIMIT = 600
        const val SOURCE_NAME = "myquran.com"
    }
}

/** Reduces "KAB. KUDUS", "Kabupaten Kudus" and "KOTA SEMARANG" to the bare name both sides can be
 * compared on. */
private fun String.normalizeKabkota(): String =
    uppercase(Locale.ROOT)
        .replace(Regex("^(KABUPATEN|KAB\\.|KAB|KOTA ADM\\.|KOTA ADMINISTRASI|KOTA)\\s+"), "")
        .replace(Regex("[^A-Z ]"), "")
        .trim()

/**
 * Cities whose name plausibly denotes [candidate].
 *
 * Exact match on the bare name first ("Kabupaten Kudus" → "KAB. KUDUS"). Failing that, a city whose
 * bare name is a leading *whole word* of the candidate ("KOTA JAKARTA" for "Kota Jakarta Barat") —
 * which is how myquran's coarser list lines up with the geocoder's finer one. Never a substring
 * match in the other direction, which would make "KAB. KEDIRI" a candidate for "Kediri Kota".
 *
 * Where both a kota and a kabupaten share a name, the candidate's own prefix decides; if it says
 * neither, both survive and the caller treats it as ambiguous.
 */
private fun List<PrayerCityEntity>.matching(candidate: String): List<PrayerCityEntity> {
    val normalized = candidate.normalizeKabkota()
    if (normalized.isBlank()) return emptyList()
    val exact = filter { it.name.normalizeKabkota() == normalized }
    val found =
        exact.ifEmpty {
            filter { city ->
                val bare = city.name.normalizeKabkota()
                bare.isNotBlank() && (normalized == bare || normalized.startsWith("$bare "))
            }
        }
    return found.preferMatchingType(candidate)
}

/** "Kabupaten Bandung" should not resolve to "KOTA BANDUNG", and vice versa. */
private fun List<PrayerCityEntity>.preferMatchingType(candidate: String): List<PrayerCityEntity> {
    if (size <= 1) return this
    val upper = candidate.uppercase(Locale.ROOT)
    val wantsKabupaten = upper.startsWith("KABUPATEN") || upper.startsWith("KAB.")
    val wantsKota = upper.startsWith("KOTA")
    val typed =
        when {
            wantsKabupaten -> filter { it.name.uppercase(Locale.ROOT).startsWith("KAB") }
            wantsKota -> filter { it.name.uppercase(Locale.ROOT).startsWith("KOTA") }
            else -> emptyList()
        }
    return typed.ifEmpty { this }
}

/** What to pre-fill the picker's search with when detection cannot pin down one city. */
private fun String.pickerQuery(): String = normalizeKabkota().lowercase(Locale.ROOT)
