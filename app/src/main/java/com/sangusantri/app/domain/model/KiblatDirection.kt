package com.sangusantri.app.domain.model

/**
 * Where the Ka'bah is from the reader: [bearingDegrees] clockwise from true north, and
 * [distanceMetres] as the great-circle distance to it.
 *
 * The distance is not decoration. The app deliberately truncates the coordinate it sends to
 * myquran to two decimals (~1.1 km, `coarseCoordinate`), so within a few kilometres of the Ka'bah
 * that truncation alone is worth tens of degrees of bearing error — [isTooCloseForCompass] is the
 * point past which the app must stop drawing a needle it cannot stand behind and tell the reader
 * to face Baitullah directly instead.
 */
data class KiblatDirection(
    val bearingDegrees: Float,
    val distanceMetres: Float,
) {
    val isTooCloseForCompass: Boolean get() = distanceMetres < COMPASS_MINIMUM_DISTANCE_METRES

    private companion object {
        const val COMPASS_MINIMUM_DISTANCE_METRES = 3_000f
    }
}
