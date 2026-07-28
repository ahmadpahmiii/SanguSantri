package com.sangusantri.app.data.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM coverage for [isRetryableHttpStatus] (section 19: HTTP status classification, retryable
 * versus permanent failure classification) — no MockWebServer or Room needed. */
class ContentSyncHttpClassificationTest {
    @Test
    fun requestTimeoutAndRateLimitAreRetryable() {
        assertTrue(isRetryableHttpStatus(408))
        assertTrue(isRetryableHttpStatus(429))
    }

    @Test
    fun serverErrorsAreRetryable() {
        assertTrue(isRetryableHttpStatus(500))
        assertTrue(isRetryableHttpStatus(503))
    }

    @Test
    fun clientErrorsOtherThan408And429ArePermanent() {
        assertFalse(isRetryableHttpStatus(400))
        assertFalse(isRetryableHttpStatus(404))
    }
}
