package com.sangusantri.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.sangusantri.app.data.location.DeviceLocationSource
import com.sangusantri.app.data.remote.prayertimes.api.PrayerTimesApiService
import com.sangusantri.app.domain.repository.KiblatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

/**
 * Qibla bearing from myquran, computed from the device's **coarse** last-known position.
 *
 * Prefers [DeviceLocationSource]'s last known coarse fix, falling back to a single timed-out
 * `getCurrentLocation` only when nothing is cached: a qibla bearing varies by well under a degree
 * across a city, so a stale coarse position is as good as a fresh precise one, and GPS is never
 * involved either way.
 *
 * The computed bearing is cached, so the compass keeps working with no network and no further
 * location reads.
 */
class KiblatRepositoryImpl
@Inject
constructor(
    private val locationSource: DeviceLocationSource,
    private val apiService: PrayerTimesApiService,
    private val dataStore: DataStore<Preferences>,
) : KiblatRepository {
    override fun observeBearing(): Flow<Float?> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.map { it[BEARING_DEGREES] }

    override suspend fun refreshBearing(): Result<Float> =
        runCatching {
            val location = locationSource.currentLocation() ?: error("no location available")
            val response = apiService.getQibla(coarseCoordinate(location.latitude, location.longitude))
            val payload = response.body()?.takeIf { response.isSuccessful && it.status }
            val direction = payload?.data?.direction ?: error("qibla unavailable")
            val bearing = direction.toFloat()
            dataStore.edit { it[BEARING_DEGREES] = bearing }
            bearing
        }

    private companion object {
        val BEARING_DEGREES = floatPreferencesKey("kiblat_bearing_degrees")
    }
}

/**
 * The position sent to myquran, truncated to [COORDINATE_DECIMALS] decimal places (~1.1 km).
 *
 * A coarse permission bounds how accurate the platform's fix is, not how many digits the app hands
 * to a third party — [android.location.Location.getLatitude] still returns full `Double` precision,
 * and this request travels in the URL path, where it is the most loggable thing about the user this
 * app ever sends anywhere (`docs/security/PRIVACY.md`). Truncating costs nothing: the bearing varies
 * by well under a degree across a whole city, which is why the app asks for coarse location at all.
 *
 * Formatted with [Locale.US] so a device on a comma-decimal locale cannot turn `-6.21` into `-6,21`
 * and corrupt the `lat,lon` path segment.
 */
internal fun coarseCoordinate(
    latitude: Double,
    longitude: Double,
): String = String.format(Locale.US, "%.${COORDINATE_DECIMALS}f,%.${COORDINATE_DECIMALS}f", latitude, longitude)

private const val COORDINATE_DECIMALS = 2
