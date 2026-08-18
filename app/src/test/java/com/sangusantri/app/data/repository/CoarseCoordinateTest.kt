package com.sangusantri.app.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * The one position this app ever sends off-device (`docs/security/PRIVACY.md`). Two properties have
 * to hold: it is truncated to city level before it leaves, and it is formatted the same way on every
 * device regardless of locale — a comma decimal separator would silently corrupt the `lat,lon` path
 * segment myquran parses.
 */
class CoarseCoordinateTest {
    @Test
    fun fullPrecisionPositionIsTruncatedToTwoDecimals() {
        assertEquals("-6.21,106.85", coarseCoordinate(-6.2087634, 106.845599))
    }

    @Test
    fun equatorAndPrimeMeridianKeepBothDecimalPlaces() {
        assertEquals("0.00,0.00", coarseCoordinate(0.0, 0.0))
    }

    @Test
    fun southernAndWesternHemispheresKeepTheirSigns() {
        assertEquals("-33.87,-151.21", coarseCoordinate(-33.8688197, -151.2092955))
    }

    @Test
    fun commaDecimalLocaleStillProducesADotSeparatedPair() {
        val original = Locale.getDefault()
        try {
            // Indonesian is the app's own primary locale and formats decimals with a comma — without
            // an explicit Locale.US this returns "-6,21,106,85", which is not a coordinate.
            Locale.setDefault(Locale.forLanguageTag("id-ID"))

            assertEquals("-6.21,106.85", coarseCoordinate(-6.2087634, 106.845599))
        } finally {
            Locale.setDefault(original)
        }
    }
}
