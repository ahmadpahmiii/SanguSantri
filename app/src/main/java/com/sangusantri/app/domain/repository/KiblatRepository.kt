package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.KiblatDirection
import kotlinx.coroutines.flow.Flow

/**
 * Where the Ka'bah is: the qibla bearing in degrees clockwise from true north, plus the distance
 * to it.
 *
 * Computed once from the device's coarse location — the bearing via myquran `/qibla/{lat},{lon}`,
 * the distance on-device — and then cached, so the compass keeps working offline. `null` means
 * nothing has ever been computed: the compass then draws no needle at all, because a needle
 * pointing at an arbitrary angle is worse than no needle.
 *
 * This is the only part of the app that uses location, and it uses the coarse permission only:
 * a qibla bearing changes by less than a degree over a whole city.
 */
interface KiblatRepository {
    fun observeDirection(): Flow<KiblatDirection?>

    /** Requires `ACCESS_COARSE_LOCATION` to have been granted; returns a failure otherwise, or when
     * no last-known position is available and the source cannot be reached. */
    suspend fun refreshDirection(): Result<KiblatDirection>
}
