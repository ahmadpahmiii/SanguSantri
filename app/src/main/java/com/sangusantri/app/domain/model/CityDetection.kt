package com.sangusantri.app.domain.model

/**
 * The outcome of turning the device's position into a prayer-schedule city.
 *
 * [Ambiguous] exists because the geocoder and myquran do not name places at the same granularity —
 * the geocoder says "Kota Jakarta Barat" where myquran has one "KOTA JAKARTA", and "Bandung" is
 * both a kota and a kabupaten. Rather than pick one and risk showing someone another city's prayer
 * times, the app hands the reader a pre-filled picker and lets them confirm.
 */
sealed interface CityDetection {
    data class Detected(
        val city: PrayerCity,
    ) : CityDetection

    /** A place name was resolved but not to exactly one city; [query] pre-fills the picker. */
    data class Ambiguous(
        val query: String,
    ) : CityDetection

    /** No permission, no position, or the geocoder returned nothing. */
    data object Unavailable : CityDetection
}
