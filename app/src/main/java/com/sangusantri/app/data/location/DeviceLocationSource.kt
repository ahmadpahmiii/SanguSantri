package com.sangusantri.app.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * The app's single point of contact with device location — used by the qibla bearing and by the
 * "detect my city" path for prayer times.
 *
 * Every call here blocks (binder IPC, and the geocoder additionally hits the network), so all of it
 * runs on [Dispatchers.IO]. Calling the geocoder from the main dispatcher is exactly the bug that
 * made city detection fail silently the first time this shipped.
 */
class DeviceLocationSource
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * A usable position, preferring whatever is already cached and asking for a fresh fix only when
     * nothing is.
     *
     * The fresh-fix path matters: an app that has just been granted location for the first time
     * usually finds *no* last-known fix, because the providers have never had reason to cache one
     * for it. Relying on the cache alone is why "detect my city" appeared to do nothing right after
     * the permission dialog.
     */
    suspend fun currentLocation(): Location? =
        withContext(Dispatchers.IO) {
            lastKnownLocation() ?: requestSingleFix()
        }

    fun lastKnownLocation(): Location? {
        val manager =
            if (hasPermission()) ContextCompat.getSystemService(context, LocationManager::class.java) else null
        // Guarded by hasPermission() above, and every read is additionally wrapped: a provider can
        // still throw SecurityException if the grant is revoked between the check and the call.
        return manager
            ?.allProviders
            ?.asSequence()
            ?.mapNotNull { provider -> runCatching { readLastKnown(manager, provider) }.getOrNull() }
            ?.maxByOrNull { it.time }
    }

    /** One fix, then done — never a stream of updates. Times out rather than leaving the caller
     * waiting on a provider that will not answer (indoors, no signal, emulator with no fix set). */
    private suspend fun requestSingleFix(): Location? {
        val manager =
            if (hasPermission()) ContextCompat.getSystemService(context, LocationManager::class.java) else null
        val provider = manager?.preferredCoarseProvider()
        return if (manager == null || provider == null) {
            null
        } else {
            withTimeoutOrNull(FIX_TIMEOUT_MILLIS) {
                suspendCancellableCoroutine { continuation ->
                    val cancellationSignal = CancellationSignal()
                    continuation.invokeOnCancellation { cancellationSignal.cancel() }
                    runCatching { requestCurrent(manager, provider, cancellationSignal, continuation) }
                        .onFailure { if (continuation.isActive) continuation.resume(null) }
                }
            }
        }
    }

    /** Coarse providers only, cheapest first: the fused one where the platform offers it, then the
     * network provider. GPS is deliberately not used — it is slow, power-hungry, and useless
     * indoors, and city-level accuracy is all either caller needs. */
    private fun LocationManager.preferredCoarseProvider(): String? {
        val candidates =
            buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
            }
        return candidates.firstOrNull { provider ->
            runCatching { allProviders.contains(provider) && isProviderEnabled(provider) }.getOrDefault(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrent(
        manager: LocationManager,
        provider: String,
        cancellationSignal: CancellationSignal,
        continuation: CancellableContinuation<Location?>,
    ) {
        val executor = Executor { command -> command.run() }
        val consumer = Consumer<Location?> { location -> if (continuation.isActive) continuation.resume(location) }
        LocationManagerCompat.getCurrentLocation(manager, provider, cancellationSignal, executor, consumer)
    }

    @SuppressLint("MissingPermission")
    private fun readLastKnown(
        manager: LocationManager,
        provider: String,
    ): Location? = manager.getLastKnownLocation(provider)

    /**
     * The kabupaten/kota name for [location], via the platform geocoder.
     *
     * myquran's schedule list is keyed by kabkota and its lookup endpoint returns no coordinates,
     * so this is the only way to turn a position into a city id. Returns the candidate names worth
     * matching (most specific first) rather than one string, because Android fills these fields
     * inconsistently across devices and regions.
     */
    @Suppress("DEPRECATION")
    suspend fun kabkotaCandidates(location: Location): List<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                Geocoder(context, Locale("id", "ID"))
                    .getFromLocation(location.latitude, location.longitude, 1)
                    .orEmpty()
                    .firstOrNull()
                    ?.let { address -> listOfNotNull(address.subAdminArea, address.locality, address.adminArea) }
                    .orEmpty()
            }.getOrDefault(emptyList())
        }

    private companion object {
        const val FIX_TIMEOUT_MILLIS = 10_000L
    }
}
