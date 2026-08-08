package com.sangusantri.app.data.remote.quran

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Header-origin isolation (ADR 0016, `docs/security/SECURITY_BASELINE.md`): the Kemenag
 * `username`/`token` headers must attach only to requests targeting [QuranAuthInterceptor.QURAN_API_HOST],
 * never to any other host. A terminal test interceptor captures the outgoing request and returns a
 * canned response instead of calling `chain.proceed()`, so no real network I/O happens.
 *
 * Instrumented (not a plain JVM test) because [QuranCredentialProvider] needs a real [android.content.Context];
 * `BuildConfig.DEBUG` is always true for this test target, so it resolves the fixed fake fixture
 * credential without ever touching the native library.
 */
@RunWith(AndroidJUnit4::class)
class QuranAuthInterceptorTest {
    private lateinit var interceptor: QuranAuthInterceptor
    private var capturedRequest: Request? = null

    @Before
    fun setUp() {
        val credentialProvider = QuranCredentialProvider(ApplicationProvider.getApplicationContext())
        interceptor = QuranAuthInterceptor(credentialProvider)
    }

    @Test
    fun attachesCredentialHeadersForKemenagHost() {
        val request = Request.Builder().url("https://${QuranAuthInterceptor.QURAN_API_HOST}/surah/local/1/114").build()

        client().newCall(request).execute().close()

        assertEquals("debug-fixture-username", capturedRequest?.header("username"))
        assertEquals("debug-fixture-token", capturedRequest?.header("token"))
    }

    @Test
    fun doesNotAttachCredentialHeadersForOtherHost() {
        val request = Request.Builder().url("https://content-api.sangusantri.invalid/content/catalog.json").build()

        client().newCall(request).execute().close()

        assertNull(capturedRequest?.header("username"))
        assertNull(capturedRequest?.header("token"))
    }

    private fun client(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                capturedRequest = chain.request()
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody(null))
                    .build()
            }.build()
}
