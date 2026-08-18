package com.sangusantri.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * The response-size cap (`docs/security/SECURITY_BASELINE.md`) is the only thing standing between a
 * compromised or misbehaving host and an unbounded read into memory, and it is installed on all four
 * OkHttp clients. These exercise both halves of it: the cheap `Content-Length` pre-check, and the
 * streaming check that has to hold when `Content-Length` is absent or lies.
 */
class ResponseSizeLimitInterceptorTest {
    private lateinit var server: MockWebServer

    private val maxBytes = 1_024L

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun bodyUnderLimitIsReturnedIntact() {
        server.enqueue(MockResponse().setBody("x".repeat(512)))

        val body = client().newCall(request()).execute().use { it.body.string() }

        assertEquals(512, body.length)
    }

    @Test
    fun bodyAtExactlyTheLimitIsAllowed() {
        server.enqueue(MockResponse().setBody("x".repeat(maxBytes.toInt())))

        val body = client().newCall(request()).execute().use { it.body.string() }

        assertEquals(maxBytes.toInt(), body.length)
    }

    @Test
    fun declaredContentLengthOverLimitIsRejectedBeforeReadingTheBody() {
        server.enqueue(MockResponse().setBody("x".repeat((maxBytes + 1).toInt())))

        // Thrown from the interceptor itself, so the oversized body is never read at all.
        assertThrows(IOException::class.java) { client().newCall(request()).execute() }
    }

    @Test
    fun chunkedBodyOverLimitIsRejectedWhileReading() {
        // No Content-Length to pre-check, which is exactly the case the streaming guard exists for:
        // an understated or absent length must not buy an unbounded read.
        server.enqueue(
            MockResponse()
                .setChunkedBody(Buffer().writeUtf8("x".repeat((maxBytes * 4).toInt())), 256),
        )

        val response = client().newCall(request()).execute()

        assertThrows(IOException::class.java) { response.use { it.body.string() } }
    }

    private fun client() =
        OkHttpClient
            .Builder()
            .addInterceptor(ResponseSizeLimitInterceptor(maxBytes))
            .build()

    private fun request() = Request.Builder().url(server.url("/catalog.json")).build()
}
