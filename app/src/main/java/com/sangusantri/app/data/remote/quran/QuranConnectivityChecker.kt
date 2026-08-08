package com.sangusantri.app.data.remote.quran

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * A synchronous connectivity check for the Quran entry gate (`docs/product/QURAN_PRD.md` §6.1):
 * "If the device is offline and no complete local dataset exists" is a distinct state from a
 * network/HTTP failure encountered mid-sync, so this is checked before ever attempting the first
 * full preparation, not inferred from a sync failure afterward.
 */
class QuranConnectivityChecker
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
) {
    @Suppress("ReturnCount")
    fun isConnected(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
