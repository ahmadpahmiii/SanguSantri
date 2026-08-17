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
import javax.inject.Inject

/**
 * Qibla bearing from myquran, computed from the device's **coarse** last-known position.
 *
 * Uses [DeviceLocationSource]'s last known coarse fix rather than requesting an active one: a qibla
 * bearing varies by well under a degree across a city, so a stale coarse position is as good as a
 * fresh precise one, and this way the app neither spins up the GPS nor gains a dependency.
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
            val coordinate = "${location.latitude},${location.longitude}"
            val response = apiService.getQibla(coordinate)
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
