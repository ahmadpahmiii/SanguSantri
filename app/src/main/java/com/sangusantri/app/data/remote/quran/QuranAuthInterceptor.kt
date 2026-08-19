package com.sangusantri.app.data.remote.quran

import com.sangusantri.app.data.remote.quran.QuranAuthInterceptor.Companion.QURAN_API_HOST
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Attaches the Kemenag `username`/`token` headers (ADR 0016 §9). This interceptor is added only to
 * the dedicated Quran OkHttp client (`di/QuranNetworkModule.kt`) — it is never installed on
 * [com.sangusantri.app.di.NetworkModule]'s shared CMS content client. As a second,
 * defensive layer it also refuses to attach credentials to a request whose host is not exactly
 * [QURAN_API_HOST], so headers can never leak to a redirect target on a different origin.
 */
class QuranAuthInterceptor
@Inject
constructor(
    private val credentialProvider: QuranCredentialProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.host != QURAN_API_HOST) {
            return chain.proceed(request)
        }
        val credential =
            credentialProvider.getCredential()
                ?: throw IOException("Kemenag credential unavailable")
        val authenticated =
            request
                .newBuilder()
                .header(HEADER_USERNAME, credential.username)
                .header(HEADER_AUTHORIZATION, credential.token)
                .build()
        return chain.proceed(authenticated)
    }

    companion object {
        const val QURAN_API_HOST = "quran-api.lpmqkemenag.id"
        private const val HEADER_USERNAME = "user"
        private const val HEADER_AUTHORIZATION = "Authorization"
    }
}
